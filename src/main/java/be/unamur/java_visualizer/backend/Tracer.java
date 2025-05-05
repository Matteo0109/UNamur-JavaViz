package be.unamur.java_visualizer.backend;

import be.unamur.java_visualizer.model.ExecutionTrace;
import be.unamur.java_visualizer.model.Frame;
import be.unamur.java_visualizer.model.HeapEntity;
import be.unamur.java_visualizer.model.HeapList;
import be.unamur.java_visualizer.model.HeapMap;
import be.unamur.java_visualizer.model.HeapObject;
import be.unamur.java_visualizer.model.HeapPrimitive;
import be.unamur.java_visualizer.model.Value;
import be.unamur.java_visualizer.plugin.MainPane;
import be.unamur.java_visualizer.ui.VisualizationPanel;
import be.unamur.java_visualizer.plugin.PluginSettings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VoidValue;
import com.sun.jdi.Method;



import java.util.*;

import static be.unamur.java_visualizer.backend.TracerUtils.displayNameForType;
import static be.unamur.java_visualizer.backend.TracerUtils.doesImplementInterface;
import static be.unamur.java_visualizer.backend.TracerUtils.getIterator;
import static be.unamur.java_visualizer.backend.TracerUtils.invokeSimple;
import be.unamur.java_visualizer.plugin.PluginSettings;


/**
 * Some code from traceprinter, written by David Pritchard (daveagp@gmail.com)
 */
public class Tracer {
	private static final String[] INTERNAL_PACKAGES = {
			"java.",
			"javax.",
			"sun.",
			"jdk.",
			"com.sun.",
			"com.intellij.",
			"be.unamur.java_visualizer.",
			"org.junit.",
			"jh61b.junit.",
			"jh61b.",
	};
	private static final List<String> BOXED_TYPES = Arrays.asList("Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "Boolean");
	private static final boolean SHOW_ALL_FIELDS = false;
	private static final List<ReferenceType> STATIC_LISTABLE = new ArrayList<>();

	private ThreadReference thread;
	private MainPane panel;
	private ExecutionTrace model;

	/*
	Converting actual heap objects requires running code on the suspended VM thread.
	However, once we start running code on the thread, we can no longer read frame locals.
	Therefore, we have to convert all heap objects at the very end.
	*/
	private TreeMap<Long, ObjectReference> pendingConversion = new TreeMap<>();

	public Tracer(ThreadReference thread, MainPane panel) {
		this.thread = thread;
		this.panel = panel;
	}
	public ExecutionTrace getModel() throws IncompatibleThreadStateException {
		model = new ExecutionTrace();

		// Convert stack frame locals
		for (StackFrame frame : thread.frames()) {
			if (shouldShowFrame(frame)) {
				model.frames.add(convertFrame(frame));
			}
		}

		// Convert (some) statics
		for (ReferenceType rt : STATIC_LISTABLE) {
			if (rt.isInitialized() && !isInternalPackage(rt.name())) {
				for (Field f : rt.visibleFields()) {
					if (f.isStatic()) {
						String name = rt.name() + "." + f.name();
						model.statics.put(name, convertValue(rt.getValue(f)));
					}
				}
			}
		}

		// Convert heap
		Set<Long> heapDone = new HashSet<>();
		while (!pendingConversion.isEmpty()) {
			Map.Entry<Long, ObjectReference> first = pendingConversion.firstEntry();
			long id = first.getKey();
			ObjectReference obj = first.getValue();
			pendingConversion.remove(id);
			if (heapDone.contains(id))
				continue;
			heapDone.add(id);
			HeapEntity converted = convertObject(obj);
			converted.id = id;
			model.heap.put(id, converted);
		}

		return model;
	}

	// TODO clean this up
	private Frame convertFrame(StackFrame sf) {
		Frame output = new Frame();
		output.name = sf.location().method().name() + ": " + sf.location().lineNumber();

		if (sf.thisObject() != null) {
			output.locals.put("this", convertValue(sf.thisObject()));
		}

		// list args first
		/* KNOWN ISSUE:
		   .arguments() gets the args which have names in LocalVariableTable,
           but if there are none, we get an IllegalArgExc, and can use .getArgumentValues()
           However, sometimes some args have names but not all. Such as within synthetic
           lambda methods like "lambda$inc$0". For an unknown reason, trying .arguments()
           causes a JDWP error in such frames. So sadly, those frames are incomplete. */

		boolean JDWPerror = false;
		try {
			sf.getArgumentValues();
		} catch (com.sun.jdi.InternalException e) {
			if (e.toString().contains("Unexpected JDWP Error:")) {
				// expect JDWP error 35
				JDWPerror = true;
			} else {
				throw e;
			}
		}

		List<LocalVariable> frame_vars, frame_args;
		boolean completed_args = false;
		try {
			// args make sense to show first
			frame_args = sf.location().method().arguments(); //throwing statement
			completed_args = !JDWPerror && frame_args.size() == sf.getArgumentValues().size();
			for (LocalVariable lv : frame_args) {
				if (lv.name().equals("args")) {
					com.sun.jdi.Value v = sf.getValue(lv);
					if (v instanceof ArrayReference && ((ArrayReference) v).length() == 0)
						continue;
				}
				try {
					Value val = convertValue(sf.getValue(lv));
					val.declarationType = lv.typeName();
					output.locals.put(lv.name(), val);
				} catch (IllegalArgumentException exc) {
					System.out.println("That shouldn't happen!");
				}
			}
		} catch (AbsentInformationException e) {
			// ok.
		}


		// args did not have names, like a functional interface call...
		// although hopefully a future Java version will give them names!
		if (!completed_args && !JDWPerror) {
			try {
				List<com.sun.jdi.Value> anon_args = sf.getArgumentValues();
				for (int i = 0; i < anon_args.size(); i++) {
					String name = "param#" + i;
					output.locals.put(name, convertValue(anon_args.get(i)));
				}
			} catch (InvalidStackFrameException e) {
				// ok.
			}
		}

		// now non-args
		try {
			/* We're using the fact that the hashCode tells us something
		   about the variable's position (which is subject to change)
		   to compensate for that the natural order of variables()
		   is often different from the declaration order (see LinkedList.java) */
			frame_vars = sf.location().method().variables();
			TreeMap<Integer, LocalVariable> orderByHash = null;
			int offset = 0;
			for (LocalVariable lv : frame_vars) {
				if (!lv.isArgument() && (SHOW_ALL_FIELDS || !lv.name().endsWith("$"))) {
					if (orderByHash == null) {
						offset = lv.hashCode();
						orderByHash = new TreeMap<>();
					}
					orderByHash.put(lv.hashCode() - offset, lv);
				}
			}
			if (orderByHash != null) {
				for (Map.Entry<Integer, LocalVariable> me : orderByHash.entrySet()) {
					try {
						LocalVariable lv = me.getValue();
						Value val = convertValue(sf.getValue(lv));
						val.declarationType = lv.typeName();
						output.locals.put(lv.name(), val);
					} catch (IllegalArgumentException exc) {
						// variable not yet defined, don't list it
					}
				}
			}
		} catch (AbsentInformationException ex) {
			// ok.
		}


		return output;
	}

	private Value convertReference(ObjectReference obj) {
		// Special handling for boxed types
		if (obj.referenceType().name().startsWith("java.lang.")
				&& BOXED_TYPES.contains(obj.referenceType().name().substring(10))) {
			return convertValue(obj.getValue(obj.referenceType().fieldByName("value")));
		}

		long key = obj.uniqueID();
		pendingConversion.put(key, obj);

		// Actually create and return the reference
		Value out = new Value();
		out.type = Value.Type.REFERENCE;
		out.reference = key;
		return out;
	}

	private HeapEntity convertObject(ObjectReference obj) {
		boolean isAbstract;
		// Récupère le VisualizationPanel depuis le MainPane
		VisualizationPanel vizPanel = (panel != null) ? panel.getVisualizationPanel() : null;
		if (vizPanel != null) {
			// Si le panel UI existe, demande lui le mode
			isAbstract = vizPanel.isAbstractView();
		} else {
			isAbstract = false; // Par défaut : mode concret si l'UI n'est pas prête
			//System.out.println("Tracer.convertObject: VisualizationPanel not ready, defaulting to concrete view."); // Log pour info
		}

		if (isAbstract) {
			// On renvoie un HeapPrimitive (comme si c'était un String) pour que l'affichage soit identique
			// à un "vrai" String.
			HeapPrimitive asString = new HeapPrimitive();
			asString.type = HeapEntity.Type.PRIMITIVE;
			if ("Simplifié".equals(PluginSettings.getTypeMode())) {
				asString.label = PluginSettings.simplifyTypeName(displayNameForType(obj));
			}
			else {
				asString.label = displayNameForType(obj);
			}
			asString.id = obj.uniqueID();
			// On stocke la vraie valeur toString() dans asString.value
			Value val = new Value();
			val.type = Value.Type.STRING;
			val.stringValue = getRealToString(obj, thread);
			asString.value = val;
			asString.isString = "String".equals(asString.label) || "java.lang.String".equals(asString.label);

			return asString;
		} else {

			if (obj instanceof ArrayReference) {
				ArrayReference ao = (ArrayReference) obj;
				int length = ao.length();

				HeapList out = new HeapList();
				out.type = HeapEntity.Type.LIST;
				out.label = ao.type().name();
				for (int i = 0; i < length; i++) {
					// TODO: optional feature, skip runs of zeros
					out.items.add(convertValue(ao.getValue(i)));
				}
				return out;

		} else if (obj instanceof StringReference) {
			String strVal = ((StringReference) obj).value();

			HeapPrimitive out = new HeapPrimitive();
			out.type  = HeapEntity.Type.PRIMITIVE;

			// En mode “Précis” on affiche le nom complet, sinon “String”
			if ("Précis".equals(PluginSettings.getTypeMode())) {
				out.label = obj.referenceType().name();        // => "java.lang.String"
			} else {
				out.label = "String";
			}

			out.value = new Value();
			out.value.type        = Value.Type.STRING;
			out.value.stringValue = strVal;
			return out;
		}


		String typeName = obj.referenceType().name();

			if ((doesImplementInterface(obj, "java.util.List")
					|| doesImplementInterface(obj, "java.util.Set"))
					&& isInternalPackage(typeName)) {
				HeapList out = new HeapList();
				out.type = HeapEntity.Type.LIST; // XXX: or SET
				if ("Simplifié".equals(PluginSettings.getTypeMode())) {
					out.label = PluginSettings.simplifyTypeName(displayNameForType(obj));
				}
				else {
					out.label = displayNameForType(obj);
				}

				Iterator<com.sun.jdi.Value> i = getIterator(thread, obj);
				while (i.hasNext()) {
					out.items.add(convertValue(i.next()));
				}
				return out;
			}

			if (doesImplementInterface(obj, "java.util.Map") && isInternalPackage(typeName)) {
				HeapMap out = new HeapMap();
				out.type = HeapEntity.Type.MAP;
				if ("Simplifié".equals(PluginSettings.getTypeMode())) {
					out.label = PluginSettings.simplifyTypeName(displayNameForType(obj));
				}
				else {
					out.label = displayNameForType(obj);
				}

				ObjectReference entrySet = (ObjectReference) invokeSimple(thread, obj, "entrySet");
				Iterator<com.sun.jdi.Value> i = getIterator(thread, entrySet);
				while (i.hasNext()) {
					ObjectReference entry = (ObjectReference) i.next();
					HeapMap.Pair pair = new HeapMap.Pair();
					pair.key = convertValue(invokeSimple(thread, entry, "getKey"));
					pair.val = convertValue(invokeSimple(thread, entry, "getValue"));
					out.pairs.add(pair);
				}
				return out;
			}

			// now, arbitrary objects
			HeapObject out = new HeapObject();
			out.type = HeapEntity.Type.OBJECT;
			if ("Simplifié".equals(PluginSettings.getTypeMode())) {
				out.label = PluginSettings.simplifyTypeName(displayNameForType(obj));
			}
			else {
				out.label = displayNameForType(obj);
			}

			ReferenceType refType = obj.referenceType();

			if (shouldShowDetails(refType)) {
				// fields: -inherited -hidden +synthetic
				// visibleFields: +inherited -hidden +synthetic
				// allFields: +inherited +hidden +repeated_synthetic
				Map<Field, com.sun.jdi.Value> fields = obj.getValues(
						SHOW_ALL_FIELDS ? refType.allFields() : refType.visibleFields()
				);
				for (Map.Entry<Field, com.sun.jdi.Value> me : fields.entrySet()) {
					if (!me.getKey().isStatic() && (SHOW_ALL_FIELDS || !me.getKey().isSynthetic())) {
						String name = SHOW_ALL_FIELDS ? me.getKey().declaringType().name() + "." : "";
						name += me.getKey().name();
						Value value = convertValue(me.getValue());
						out.fields.put(name, value);
					}
				}
			}
			return out;
		}
	}

	private Value convertValue(com.sun.jdi.Value v) {
		Value out = new Value();
		if (v instanceof BooleanValue) {
			out.type = Value.Type.BOOLEAN;
			out.booleanValue = ((BooleanValue) v).value();
			out.typeName = "bool";
		} else if (v instanceof ByteValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((ByteValue) v).value();
			out.typeName = "byte";
		} else if (v instanceof ShortValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((ShortValue) v).value();
			out.typeName = "short";
		} else if (v instanceof IntegerValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((IntegerValue) v).value();
			out.typeName = "int";
		} else if (v instanceof LongValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((LongValue) v).value();
			out.typeName = "long";
		} else if (v instanceof FloatValue) {
			out.type = Value.Type.DOUBLE;
			out.doubleValue = ((FloatValue) v).value();
			out.typeName = "float";
		} else if (v instanceof DoubleValue) {
			out.type = Value.Type.DOUBLE;
			out.doubleValue = ((DoubleValue) v).value();
			out.typeName = "double";
		} else if (v instanceof CharValue) {
            out.type = Value.Type.CHAR;
			out.charValue = ((CharValue) v).value();
			out.typeName = "char";
		} else if (v instanceof VoidValue) {
			out.type = Value.Type.VOID;
			out.typeName = "void";
		} else if (!(v instanceof ObjectReference)) {
			out.type = Value.Type.NULL;
			out.typeName = ((ObjectReference) v).referenceType().name();
		} else if (v instanceof ObjectReference) {
			// Qu'il s'agisse d'un StringReference ou d'un autre objet,
			// on le traite comme un objet
			out = convertReference((ObjectReference) v);
			out.typeName = ((ObjectReference) v).referenceType().name();

		} else {
			out.type = Value.Type.NULL;
		}
		return out;
	}

	// input format: [package.]ClassName:lineno or [package.]ClassName
	private static boolean isInternalPackage(final String name) {
		return Arrays.stream(INTERNAL_PACKAGES).anyMatch(name::startsWith);
	}

	private static boolean shouldShowFrame(StackFrame frame) {
		Location loc = frame.location();
		return !isInternalPackage(loc.toString()) && !loc.method().name().contains("$access");
	}

	private static boolean shouldShowDetails(ReferenceType type) {
		return !isInternalPackage(type.name());
	}

	private String getRealToString(ObjectReference obj, ThreadReference thread) {
		try {
			List<Method> toStringMethods = obj.referenceType().methodsByName("toString", "()Ljava/lang/String;");
			if (!toStringMethods.isEmpty()) {
				Method m = toStringMethods.get(0);

				// Invocation de toString() dans la VM.
				com.sun.jdi.Value retVal = obj.invokeMethod(
						thread, m,
						Collections.emptyList(),
						ObjectReference.INVOKE_SINGLE_THREADED);
				if (retVal instanceof StringReference) {
					return ((StringReference) retVal).value();
				}
			}
		} catch (Exception e) {
			// En cas d'erreur, on renvoie un fallback.
			return obj.toString();
		}
		// Si aucune méthode trouvée, fallback :
		return obj.toString();
	}

}






















