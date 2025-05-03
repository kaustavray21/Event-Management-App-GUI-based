import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EventManagerGUI extends JFrame {
    private final EventManagerApp app;
    private JTextArea outputArea;
    private JTable eventTable;
    private EventTableModel tableModel;

    public EventManagerGUI() {
        app = new EventManagerApp();
        app.loadEvents(); // Load existing events

        setTitle("Event Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create menu bar
        createMenuBar();

        // Create components
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Create table
        tableModel = new EventTableModel(app.getAllEvents());
        eventTable = new JTable(tableModel);
        eventTable.setAutoCreateRowSorter(true);
        JScrollPane tableScrollPane = new JScrollPane(eventTable);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Table View", tableScrollPane);
        tabbedPane.addTab("Text View", scrollPane);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        String[] buttonLabels = {
                "Manage Events", "Manage Attendees",
                "View All Events", "View Event Attendees",
                "Filter by Type", "Save Events",
                "Exit"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        // Add components to frame
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshEventTable();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Events");
        saveItem.addActionListener(e -> saveEvents());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addEventItem = new JMenuItem("Add Event");
        addEventItem.addActionListener(e -> manageEventsDialog());
        JMenuItem deleteEventItem = new JMenuItem("Delete Event");
        deleteEventItem.addActionListener(e -> deleteSelectedEvent());
        editMenu.add(addEventItem);
        editMenu.add(deleteEventItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void refreshEventTable() {
        tableModel.setEvents(app.getAllEvents());
    }

    private void saveEvents() {
        app.saveEvents();
        JOptionPane.showMessageDialog(this, "Events saved successfully!");
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Event Management System\nVersion 1.0", "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void manageEventsDialog() {
        String[] options = {"Create Event", "Delete Event", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select an action:",
                "Event Management",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) { // Create
            JPanel panel = new JPanel(new GridLayout(4, 2));
            JTextField titleField = new JTextField();
            JTextField dateField = new JTextField();
            JTextField locationField = new JTextField();
            JTextField typeField = new JTextField();

            panel.add(new JLabel("Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Date (YYYY-MM-DD):"));
            panel.add(dateField);
            panel.add(new JLabel("Location:"));
            panel.add(locationField);
            panel.add(new JLabel("Type:"));
            panel.add(typeField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Create New Event", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                if (!app.isValidEvent(titleField.getText())) {
                    JOptionPane.showMessageDialog(this,
                            "Title cannot be empty!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!app.isValidDate(dateField.getText())) {
                    JOptionPane.showMessageDialog(this,
                            "Date must be in YYYY-MM-DD format!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                app.createEvent(
                        titleField.getText(),
                        dateField.getText(),
                        locationField.getText(),
                        typeField.getText()
                );
                refreshEventTable();
                outputArea.setText(app.getEventDetailsDisplay());
            }
        }
        else if (choice == 1) { // Delete
            deleteSelectedEvent();
        }
    }

    private void deleteSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = eventTable.convertRowIndexToModel(selectedRow);
            Event event = app.getAllEvents().get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete event: " + event.getTitle() + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                app.deleteEvent(event.getTitle());
                refreshEventTable();
                outputArea.setText(app.getEventDetailsDisplay());
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an event to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void manageAttendeesDialog() {
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No events available. Create an event first.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedEvent = (String) JOptionPane.showInputDialog(this,
                "Select an event:",
                "Manage Attendees",
                JOptionPane.QUESTION_MESSAGE,
                null,
                app.getEventTitles(),
                app.getEventTitles()[0]);

        if (selectedEvent != null) {
            String[] options = {"Add Attendee", "Remove Attendee", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Select an action:",
                    "Attendee Management for " + selectedEvent,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) { // Add
                JPanel panel = new JPanel(new GridLayout(2, 2));
                JTextField nameField = new JTextField();
                JTextField emailField = new JTextField();

                panel.add(new JLabel("Name:"));
                panel.add(nameField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);

                int result = JOptionPane.showConfirmDialog(this,
                        panel, "Add Attendee", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    if (!app.isValidEmail(emailField.getText())) {
                        JOptionPane.showMessageDialog(this,
                                "Invalid email format!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean success = app.registerAttendee(
                            selectedEvent,
                            nameField.getText(),
                            emailField.getText()
                    );

                    if (success) {
                        refreshEventTable();
                        outputArea.setText(app.getEventDetailsDisplay());
                    }
                }
            }
            else if (choice == 1) { // Remove
                List<Attendee> attendees = app.getEventAttendees(selectedEvent);
                if (attendees.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No attendees to remove.", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] attendeeNames = attendees.stream()
                        .map(a -> a.getName() + " <" + a.getEmail() + ">")
                        .toArray(String[]::new);

                String selectedAttendee = (String) JOptionPane.showInputDialog(this,
                        "Select attendee to remove:",
                        "Remove Attendee",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        attendeeNames,
                        attendeeNames[0]);

                if (selectedAttendee != null) {
                    int index = Arrays.asList(attendeeNames).indexOf(selectedAttendee);
                    boolean success = app.removeAttendee(selectedEvent, index);
                    if (success) {
                        refreshEventTable();
                        outputArea.setText(app.getEventDetailsDisplay());
                    }
                }
            }
        }
    }

    private void viewAttendeesDialog() {
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No events available.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedEvent = (String) JOptionPane.showInputDialog(this,
                "Select an event:",
                "View Attendees",
                JOptionPane.QUESTION_MESSAGE,
                null,
                app.getEventTitles(),
                app.getEventTitles()[0]);

        if (selectedEvent != null) {
            List<Attendee> attendees = app.getEventAttendees(selectedEvent);
            if (attendees.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No attendees for this event.", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create table model
            String[] columnNames = {"Name", "Email"};
            Object[][] data = attendees.stream()
                    .map(a -> new Object[]{a.getName(), a.getEmail()})
                    .toArray(Object[][]::new);

            JTable attendeeTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(attendeeTable);
            attendeeTable.setFillsViewportHeight(true);

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Attendees for " + selectedEvent,
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void filterByTypeDialog() {
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No events available to filter.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> types = app.getAllEvents().stream()
                .map(Event::getType)
                .distinct()
                .toList();

        String selectedType = (String) JOptionPane.showInputDialog(this,
                "Select event type:",
                "Filter by Type",
                JOptionPane.QUESTION_MESSAGE,
                null,
                types.toArray(),
                types.get(0));

        if (selectedType != null) {
            List<Event> filteredEvents = app.findEventsByType(selectedType);
            if (filteredEvents.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No events of this type.", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            EventTableModel filteredModel = new EventTableModel(filteredEvents);
            JTable filteredTable = new JTable(filteredModel);
            JScrollPane scrollPane = new JScrollPane(filteredTable);
            filteredTable.setFillsViewportHeight(true);

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Events of type: " + selectedType,
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Manage Events" -> manageEventsDialog();
                case "Manage Attendees" -> manageAttendeesDialog();
                case "View All Events" -> outputArea.setText(app.getEventDetailsDisplay());
                case "View Event Attendees" -> viewAttendeesDialog();
                case "Filter by Type" -> filterByTypeDialog();
                case "Save Events" -> saveEvents();
                case "Exit" -> System.exit(0);
            }
        }
    }
}