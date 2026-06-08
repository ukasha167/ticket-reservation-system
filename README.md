# Ticket Reservation System

This repository contains an event lifecycle and seating transaction processor engineered in Java utilizing the Swing/AWT ecosystem. The application manages real-time layout structures and booking validation states across distinct category domains.

---

## Technical Architecture: Design Justifications

### 1. Transactional History Tracking (Undo / Redo)

* **The "Why":** Implementing state changes in a real-time reservation grid requires a reliable mechanism to revert or re-apply mutations without corrupting the layout state or risking reference leakage.
* **The "How":** The system handles transaction logs using twin `Stack<Seat>` memory allocations tracking Last-In, First-Out (LIFO) semantics. When an entity state changes, a deep copy clone of the `Seat` object is pushed to the `undoStack`. Invoking an undo pops the record, pushes the current state to the `redoStack`, and restores the previous variables. The redo stack is cleared upon any new direct mutation to preserve linear history tracking and prevent state divergence.

### 2. State Persistence Engine

* **The "Why":** Lightweight architectural constraints required a data persistence model that avoids the memory footprint and operational complexity of external database drivers, while maintaining data durability across application restarts.
* **The "How":** The application relies on native Java Binary Serialization (`ObjectOutputStream` and `ObjectInputStream`). The engine writes tracking graphs directly to event-specific destination boundaries (`savedFiles/[eventName].dat`). At initialization, the system runs an existential check against the targeted path; if present, it deserializes the byte stream into the live operational layout (`LinkedList<Seat>`), otherwise falling back to an incremental coordinate generation routine.

### 3. Structural Presentation Refactoring

* **The "Why":** The initial development iteration duplicated UI compilation logic and layout setups across three identical visual views, violating the DRY (Don't Repeat Yourself) principle and creating high maintenance debt.
* **The "How":** Redundant layout logic was consolidated into a single parameter-driven system handler (`openCategoryUI`). This controller dynamically injects file paths, runtime contexts, window titles, and data boundaries to cleanly isolate file I/O and display setup operations.

### 4. Low-Level Graphics Pipeline Customization

* **The "Why":** Standard Java Swing buttons and borders do not inherently enforce clipping masks on internal child structures (like background images). This limitation causes pixel distortion or alignment overflow along rounded component edges.
* **The "How":** To fix this, the interface layer bypasses standard `ImageIcon` components by extending `JButton` into a custom class (`RoundedImageButton`) and overriding the `paintComponent` rendering loop. The implementation forces a mathematical clipping path configuration (`RoundRectangle2D.Float`) directly onto the `Graphics2D` context layer. This guarantees pixel-perfect asset alignment and scaling inside the designated layout dimensions.

---

## Directory Schema

```text
└── ticket-reservation/
    ├── dataset/            # Flat-file database for event configurations
    ├── imgs/               # Raw UI design system visual assets
    ├── savedFiles/         # Serializable transaction state histories
    └── src/                # Functional compilation code
        ├── App.java        # Central view layout engine and callback handlers
        ├── Main.java       # Execution entry point
        ├── Seat.java       # Serializable seat data model
        └── Ticket.java     # Immutable target ticket definition

```

---

## Execution Guide

### Prerequisite Environment

* **Java Development Kit (JDK):** Version 11 or higher.
* **Compiler Path Access:** Ensure `javac` and `java` are available in your global execution parameters.

### Build and Launch Pipeline

Execute the following instructions from the root execution directory (`ticket-reservation`):

```bash
# 1. Compile the complete dependency source tree into a separate binary directory
javac -d bin src/*.java

# 2. Execute the application entry point using the generated classpath environment
java -cp bin Main

```
