import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class App {
    final int rows = 7;
    final int cols = 5;

    final int APP_WIDTH = 1280;
    final int APP_HEIGHT = 720;

    private String[] buttonLabels = { "Cinema", "Concert", "Tournaments", "Exit" };
    private int buttonWidth = (int) (APP_WIDTH * 0.20);
    private int startX;
    private int startY;

    JButton[] buttons;

    JFrame homeWindow;
    JPanel hiddenWindow;
    Image homeWindowImg;

    Image cinemaImg;
    Image concertImg;
    Image tournamentImg;
    Image exitImg;

    int iconWidth = (int) (APP_WIDTH * 0.15);
    int iconHeight = 90;

    ArrayList<Ticket> tickets;
    ArrayList<Seat> seats;

    Stack<Seat> undoStack = new Stack<>();
    Stack<Seat> redoStack = new Stack<>();

    class RoundedImageButton extends JButton {
        private Image img;
        private int radius;

        public RoundedImageButton(Image img, int radius) {
            this.img = img;
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.geom.RoundRectangle2D.Float shape = new java.awt.geom.RoundRectangle2D.Float(
                    0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2d.setClip(shape);
            g2d.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            if (getModel().isArmed()) {
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fill(shape);
            }
            g2d.setClip(null);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(shape);
            g2d.dispose();
        }
    }

    public App() {
        initializeGUI();
    }

    public void initializeGUI() {
        homeWindowImg = new ImageIcon("imgs/homeWindowImg.jpg").getImage();
        homeWindow = new JFrame();
        homeWindow.setTitle("Ticket Management System");
        homeWindow.setSize(APP_WIDTH, APP_HEIGHT);
        homeWindow.setLocationRelativeTo(null);
        homeWindow.setResizable(false);
        homeWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buttons = new JButton[4];

        cinemaImg = new ImageIcon("imgs/cinema.png").getImage();
        concertImg = new ImageIcon("imgs/concert.png").getImage();
        tournamentImg = new ImageIcon("imgs/tournament.png").getImage();
        exitImg = new ImageIcon("imgs/exit.png").getImage();

        startX = 25 + (int) (APP_WIDTH * 0.3) / 2 - buttonWidth / 2;
        startY = (APP_HEIGHT - (4 * 90 + 3 * 15) - (30)) / 2;

        JPanel mainPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(homeWindowImg, 0, 0, APP_WIDTH, APP_HEIGHT, null);
            }
        };
        mainPanel.setLayout(null);

        hiddenWindow = new JPanel();
        hiddenWindow.setBounds((int) (APP_WIDTH * 0.3), 0, (int) (APP_WIDTH * 0.7), APP_HEIGHT);
        hiddenWindow.setOpaque(false);

        Image[] images = { cinemaImg, concertImg, tournamentImg, exitImg };

        for (int i = 0; i < images.length; i++) {
            buttons[i] = new RoundedImageButton(images[i], 15);
            buttons[i].setActionCommand(buttonLabels[i]);
            buttons[i].setBounds(startX, startY + i * (iconHeight + 20), iconWidth, iconHeight);
            buttons[i].addActionListener(new ButtonListener());
            mainPanel.add(buttons[i]);
        }

        mainPanel.add(hiddenWindow);
        homeWindow.add(mainPanel);
        homeWindow.setVisible(true);
    }

    public void loadTickets(String category) throws Exception {
        File file;
        if (category.equals("movies")) {
            file = new File("dataset/movies.txt");
        } else if (category.equals("concerts")) {
            file = new File("dataset/concerts.txt");
        } else if (category.equals("tournaments")) {
            file = new File("dataset/tournaments.txt");
        } else {
            throw new IllegalArgumentException("Invalid category selected.");
        }

        if (!file.exists()) {
            throw new FileNotFoundException("Database file not found: " + file.getPath());
        }

        Scanner input = new Scanner(file);
        tickets = new ArrayList<>();
        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ");
            if (parts.length != 3) {
                input.close();
                throw new IOException("Data corruption detected in file.");
            }
            tickets.add(new Ticket(parts[0], parts[1], Integer.parseInt(parts[2])));
        }
        input.close();
    }

    @SuppressWarnings("unchecked")
    public void loadSeats(String fileName) {
        seats = new ArrayList<>();
        File file = new File("savedFiles/" + fileName + ".dat");

        if (file.exists()) {
            try (ObjectInputStream read = new ObjectInputStream(new FileInputStream(file))) {
                seats = (ArrayList<Seat>) read.readObject();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(homeWindow, "Failed to load seat data.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    seats.add(new Seat(i + "" + j));
                }
            }
        }
    }

    public void saveSeatsToFile(ArrayList<Seat> seats, String title) {
        try {
            new File("savedFiles").mkdirs();
            try (ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream("savedFiles/" + title + ".dat"))) {
                write.writeObject(seats);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(homeWindow, "Failed to save booking.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCategoryUI(String datasetCategory, String frameTitleStr) {
        try {
            loadTickets(datasetCategory);
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(homeWindow, e1.getMessage(), "Runtime Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame ticketsFrame = new JFrame(frameTitleStr);
        ticketsFrame.setSize(600, 400);
        ticketsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ticketsFrame.setLayout(new BorderLayout());

        JPanel ticketListPanel = new JPanel();
        ticketListPanel.setLayout(new BoxLayout(ticketListPanel, BoxLayout.Y_AXIS));
        ticketListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (Ticket t : tickets) {
            String name = t.title;
            String time = t.times;
            String fare = String.valueOf(t.fare);

            JPanel ticketPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            ticketPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            ticketPanel.setBackground(Color.WHITE);

            ticketPanel.add(new JLabel(name.replace("_", " ")));
            ticketPanel.add(new JLabel(time));
            ticketPanel.add(new JLabel(fare));

            ticketPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    loadSeats(name);
                    JFrame seatFrame = new JFrame(frameTitleStr + ": " + name + " | Time: " + time + " | Fare: " + fare);
                    seatFrame.setSize(600, 550);
                    seatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    seatFrame.setLayout(new BorderLayout());

                    JPanel seatPanel = new JPanel(new GridLayout(rows, cols, 10, 10));
                    seatPanel.setBackground(Color.WHITE);
                    seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    JButton[][] seatButtons = new JButton[rows][cols];

                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            int index = row * cols + col;
                            Seat seat = seats.get(index);

                            JButton seatBtn = new JButton(seat.id);
                            seatBtn.setPreferredSize(new Dimension(60, 60));
                            seatBtn.setBackground(Color.LIGHT_GRAY);
                            seatBtn.setFocusPainted(false);
                            seatBtn.putClientProperty("seat", seat);
                            seatButtons[row][col] = seatBtn;

                            if (seat.booked) {
                                seatBtn.setForeground(Color.RED);
                            }

                            seatBtn.addActionListener(ev -> {
                                JButton sourceBtn = (JButton) ev.getSource();
                                Seat s = (Seat) sourceBtn.getClientProperty("seat");

                                if (!s.booked) {
                                    JFrame form = new JFrame("Book Seat: " + s.id);
                                    form.setSize(350, 350);

                                    JPanel formContent = new JPanel(new GridLayout(5, 2, 10, 10));
                                    formContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                                    formContent.add(new JLabel("Price:"));
                                    JTextField priceField = new JTextField("Rs. " + fare);
                                    priceField.setEditable(false);
                                    formContent.add(priceField);

                                    formContent.add(new JLabel("Name:"));
                                    JTextField nameField = new JTextField();
                                    formContent.add(nameField);

                                    formContent.add(new JLabel("CNIC (13 digits):"));
                                    JTextField cnicField = new JTextField();
                                    formContent.add(cnicField);

                                    formContent.add(new JLabel("Phone (11 digits):"));
                                    JTextField phoneField = new JTextField();
                                    formContent.add(phoneField);

                                    JButton confirm = new JButton("Confirm Booking");
                                    confirm.addActionListener(e2 -> {
                                        String inputName = nameField.getText();
                                        String inputCnic = cnicField.getText();
                                        String inputPhone = phoneField.getText();

                                        if (!ValidationUtils.isValidName(inputName)) {
                                            JOptionPane.showMessageDialog(form, "Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }
                                        if (!ValidationUtils.isValidCNIC(inputCnic)) {
                                            JOptionPane.showMessageDialog(form, "Invalid CNIC. Must be 13 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }
                                        if (!ValidationUtils.isValidPhone(inputPhone)) {
                                            JOptionPane.showMessageDialog(form, "Invalid Phone. Must be 11 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }

                                        s.booked = true;
                                        s.name = inputName;
                                        s.cnic = inputCnic;
                                        s.phone = inputPhone;

                                        seatBtn.setForeground(Color.RED);
                                        saveSeatsToFile(seats, name);

                                        undoStack.push(new Seat(s));
                                        redoStack.clear();
                                        form.dispose();
                                    });

                                    formContent.add(new JLabel());
                                    formContent.add(confirm);

                                    form.add(formContent);
                                    form.setLocationRelativeTo(hiddenWindow);
                                    form.setVisible(true);

                                } else {
                                    JFrame info = new JFrame("Seat Booked");
                                    info.setSize(350, 250);

                                    JPanel infoContent = new JPanel(new GridLayout(4, 1, 10, 10));
                                    infoContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                                    infoContent.add(new JLabel("Seat: " + s.id));
                                    infoContent.add(new JLabel("Booked by: " + s.name));
                                    infoContent.add(new JLabel("Phone: " + s.phone));

                                    JButton cancel = new JButton("Cancel Booking");
                                    cancel.addActionListener(e3 -> {
                                        Seat temp = new Seat(s.id);
                                        temp.booked = s.booked;
                                        temp.name = s.name;
                                        temp.phone = s.phone;
                                        temp.cnic = s.cnic;
                                        redoStack.push(temp);

                                        s.booked = false;
                                        s.name = "";
                                        s.phone = "";
                                        s.cnic = "";
                                        sourceBtn.setForeground(Color.BLACK);
                                        saveSeatsToFile(seats, name);
                                        info.dispose();
                                    });

                                    infoContent.add(cancel);
                                    info.add(infoContent);
                                    info.setLocationRelativeTo(hiddenWindow);
                                    info.setVisible(true);
                                }
                            });
                            seatPanel.add(seatBtn);
                        }
                    }

                    JButton undoButton = new JButton("Undo Booking");
                    undoButton.addActionListener(ev -> {
                        if (!undoStack.isEmpty()) {
                            Seat last = undoStack.pop();
                            for (Seat seat : seats) {
                                if (seat.id.equals(last.id)) {
                                    seat.booked = false;
                                    seat.name = "";
                                    seat.cnic = "";
                                    seat.phone = "";
                                    break;
                                }
                            }
                            redoStack.push(new Seat(last));

                            for (int i = 0; i < rows; i++) {
                                for (int j = 0; j < cols; j++) {
                                    JButton btn = seatButtons[i][j];
                                    if (btn.getText().equals(last.id)) {
                                        btn.setForeground(Color.BLACK);
                                        break;
                                    }
                                }
                            }
                        }
                    });

                    JButton redoButton = new JButton("Redo Booking");
                    redoButton.addActionListener(ev -> {
                        if (!redoStack.isEmpty()) {
                            Seat last = redoStack.pop();
                            for (Seat seat : seats) {
                                if (seat.id.equals(last.id)) {
                                    seat.booked = true;
                                    seat.name = last.name;
                                    seat.cnic = last.cnic;
                                    seat.phone = last.phone;
                                    break;
                                }
                            }
                            undoStack.push(new Seat(last));

                            for (int i = 0; i < rows; i++) {
                                for (int j = 0; j < cols; j++) {
                                    JButton btn = seatButtons[i][j];
                                    if (btn.getText().equals(last.id)) {
                                        btn.setForeground(Color.RED);
                                        break;
                                    }
                                }
                            }
                        }
                    });

                    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    bottomPanel.add(undoButton);
                    bottomPanel.add(redoButton);

                    JScrollPane seatScrollPane = new JScrollPane(seatPanel);
                    seatScrollPane.setBorder(null);
                    seatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

                    seatFrame.add(seatScrollPane, BorderLayout.CENTER);
                    seatFrame.add(bottomPanel, BorderLayout.SOUTH);
                    seatFrame.setLocationRelativeTo(hiddenWindow);
                    seatFrame.setVisible(true);
                }
            });
            ticketListPanel.add(ticketPanel);
        }

        JScrollPane listScrollPane = new JScrollPane(ticketListPanel);
        listScrollPane.setBorder(null);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        ticketsFrame.add(listScrollPane, BorderLayout.CENTER);
        ticketsFrame.setLocationRelativeTo(hiddenWindow);
        ticketsFrame.setVisible(true);
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Exit")) {
                System.exit(0);
            } else if (e.getActionCommand().equals("Cinema")) {
                openCategoryUI("movies", "Cinema Tickets");
            } else if (e.getActionCommand().equals("Concert")) {
                openCategoryUI("concerts", "Concert Tickets");
            } else if (e.getActionCommand().equals("Tournaments")) {
                openCategoryUI("tournaments", "Tournament Tickets");
            }
        }
    }
}
