# JavaViz IntelliJ Plugin

## Overview

JavaViz is an IntelliJ IDEA plugin designed to help visualize the state of a Java application during debugging. It provides a graphical representation of the call stack and the objects currently residing on the heap, displayed as a box-and-pointer diagram. This allows for a clearer understanding of program state, variable references, and object relationships.

## Prerequisites

Before you can build and run this plugin, you need the following installed on your system:

1.  **Java Development Kit (JDK):** Version 17 or later is required. Make sure your `JAVA_HOME` environment variable is set correctly.
2.  **IntelliJ IDEA (Community or Ultimate):** Version 2021.3 or later is recommended (the plugin targets `sinceBuild='193.*'` but newer versions are better tested).
3.  **Gradle:** While the project uses the Gradle Wrapper (`gradlew`), having Gradle installed can sometimes be helpful for troubleshooting. The wrapper will automatically download the correct Gradle version needed for the build.

## Setup and Building

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Matteo0109/UNamur-JavaViz.git
    cd <your-project-directory>
    ```

2.  **Open the Project in IntelliJ IDEA:**
    *   Launch your main IntelliJ IDEA installation.
    *   Select `File` > `Open...`
    *   Navigate to the directory where you cloned the repository and select it.
    *   Choose `Open as Project`.
    *   IntelliJ should automatically detect it's a Gradle project. Allow it to import and sync the project. This might take a few minutes as it downloads Gradle itself and the project dependencies (including the IntelliJ Platform SDK).

3.  **Build the Plugin:**
    *   You can build the plugin using the Gradle tool window in IntelliJ (`View` > `Tool Windows` > `Gradle`).
    *   Navigate to `Tasks` > `build`. Double-click the `build` task.
    *   Alternatively, run `./gradlew build` (or `gradlew.bat build` on Windows) in your terminal from the project's root directory.
    *   This will compile the code and create the plugin artifact (usually a `.jar` or `.zip` file) in the `build/libs` directory.

## Running the Plugin for Development (`runIde` Task)

The easiest way to test the plugin during development is to launch a *separate instance* of IntelliJ IDEA (a "sandbox" instance) with your plugin installed. This is done using the `runIde` Gradle task.

1.  **Find the `runIde` Task:**
    *   Open the Gradle tool window (`View` > `Tool Windows` > `Gradle`).
    *   Navigate to `Tasks` > `intellij`.
    *   You should see the `runIde` task.

2.  **Run `runIde`:**
    *   Double-click the `runIde` task.
    *   Alternatively, run `./gradlew runIde` (or `gradlew.bat runIde`) in your terminal.

3.  **Wait for the Sandbox IDE:**
    *   Gradle will build the plugin (if needed) and then launch a new, separate IntelliJ IDEA window. This new instance is the "sandbox" environment. It will have a distinct configuration and will automatically load your `JavaViz` plugin.
    *   **Note:** The *first* launch might be slow, as it needs to download parts of the IDE, set up the sandbox environment, and index the JDK/libraries used within the sandbox. Subsequent launches are usually faster (though indexing might still occur if caches are invalidated or dependencies change). *Performance issues related to indexing or Antivirus interference (especially on Windows) can sometimes cause significant delays.*

## Using the Plugin

1.  **Open or Create a Java Project:** Inside the **sandbox** IntelliJ IDEA instance (the one launched by `runIde`), open any simple Java project you want to debug.

2.  **Set a Breakpoint:** Place a breakpoint in your Java code where you want to inspect the state (e.g., inside a method after some objects have been created or modified).

3.  **Start Debugging:** Run your Java application in Debug mode (e.g., right-click the main class/method and select `Debug 'YourApp.main()'` or use the debug icon).

4.  **Hit the Breakpoint:** When the execution pauses at your breakpoint, the Debug tool window should appear at the bottom.

5.  **Open the JavaViz Tab:** Look for the **`JavaViz`** tab within the Debug tool window (alongside "Debugger", "Console", etc.). Click on it.

6.  **View the Visualization:**
    *   The JavaViz panel will now display the current call **Stack** on the left and the relevant objects on the **Heap** on the right.
    *   Follow the **arrows** (pointers) from variables or object fields to the objects they reference on the heap.
    *   Use the **Zoom In/Out** buttons (`+`/`-` magnifying glass icons) in the JavaViz toolbar to adjust the view size.
    *   Click the **Settings** icon (gear) in the JavaViz toolbar to change display modes (Abstract/Concrete), stack sorting, and type name simplification. The view will update automatically on the next debugger step or pause.

7.  **Step Through Code:** Use the standard debugger controls (Step Over, Resume, etc.). Each time the debugger pauses again, the JavaViz panel will automatically refresh to show the updated stack and heap state.
