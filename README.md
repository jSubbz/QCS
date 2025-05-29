QCS

QCS (Quantum Circuit Simulator) is a JavaFX-based desktop application developed for educational and practical exploration of quantum circuits. The current version focuses on UI construction, localization, theme selection, and grid-based quantum circuit manipulation. Future updates will expand its simulation capabilities.

Features

Localization and Themes
- Language Support: English, German, French.
- Theme Support: Dark mode, light mode, and accessibility-friendly options (protanopia, deuteranopia, tritanopia, achromatopsia).
- Language and theme preferences can be dynamically updated via the settings window.

UI Panels
- Menu Bar: File, settings, and help menus with actions like:
  - New Project: Clear the quantum circuit grid.
  - Open: Load a circuit pattern from a file.
  - Save: Save the current pattern.
  - Exit: Close the application.
  - Settings: Open the settings window.
  - Help: Access placeholder content for "About" and "ReadMe."
- Left Panel: Buttons to select single and multi-qubit quantum gates (e.g., X, Y, Z, H, S, T, U, CX, SWAP, etc.), phase parameter fields (A, B, C), and a barrier button.
- Top Bar: Basic window controls (currently with placeholder actions for minimize and maximize; close is functional).
- Quantum Circuit Panel:
  - Dynamic grid construction with adjustable qubits and steps.
  - Ability to place selected gates into grid cells.
  - Save/load grid patterns to/from files.
  - Action buttons for resizing, resetting, stepping through, and starting new circuits (simulation functions to be defined later).
- Bottom Panel: Status message box displaying updates and actions, tensor product display (currently static).

Resource Bundle
- Localization strings are stored in external .properties files (messages_en.properties, messages_de.properties, messages_fr.properties) for easy expansion.

Event Handling
- Central EventBus class facilitates communication between components.
- Events handled include language changes, mode toggles, theme changes, and gate selection.

Architecture Overview
[Communications Flowchart Image: src/qcs/resources/communications_flowchart.png]

Future Features (To Be Defined)
- Quantum circuit simulation: Execution of placed gates and visualization of results.
- Quantum circuit step execution: Step-wise gate execution and state updates.
- Enhanced persistence: Support for session autosave, versioning, and export options.
- Visualization tools: Circuit previews, state vector visualization, and performance metrics.
- Help and About sections: Comprehensive user guides and application information.

Building and Running
Ensure you have JavaFX configured in your environment. Use the provided build scripts (JAP-Script-JFX-bat.bat) or integrate with your preferred IDE (e.g., IntelliJ IDEA).

Example run command (adjust paths as needed):
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar JAPLabs.jar
