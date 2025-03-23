package be.unamur.java_visualizer.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class HeapObject extends HeapEntity {
	public Map<String, Value> fields = new TreeMap<>();
	private String abstractSummary; // pour stocker le résultat de toString() réel


	@Override
	public boolean hasSameStructure(HeapEntity other) {
		if (other instanceof HeapObject) {
			return fields.size() == ((HeapObject) other).fields.size();
		}
		return false;
	}

	@Override
	JSONObject toJson() {
		JSONObject o = super.toJson();
		o.put("keys", fields.keySet());
		o.put("vals", fields.values().stream().map(f -> f.toJson()).toArray());
		return o;
	}

	static HeapObject fromJson(JSONObject o) {
		HeapObject e = new HeapObject();

		JSONArray keys = o.getJSONArray("keys");
		JSONArray vals = o.getJSONArray("vals");
		for (int i = 0; i < keys.length(); i++) {
			e.fields.put(keys.getString(i), Value.fromJson(vals.getJSONArray(i)));

		}
		return e;
	}


	public void setAbstractSummary(String s) {
		this.abstractSummary = s;
	}

	public String getAbstractSummary() {
		return abstractSummary;
	}

	@Override
	public String toString() {
		// Si le résumé abstrait a été défini, le retourner
		if (abstractSummary != null) {
			return abstractSummary;
		}
		// Sinon, renvoyer par défaut le label
		return label;
	}
}
