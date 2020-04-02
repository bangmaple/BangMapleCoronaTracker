/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bangmaple.bangmaplecoronatracker.controller;

import com.bangmaple.bangmaplecoronatracker.dao.CoronaDAO;
import com.bangmaple.bangmaplecoronatracker.dto.Corona;
import com.bangmaple.bangmaplecoronatracker.util.CoronaClientAPI;
import com.bangmaple.bangmaplecoronatracker.util.MotionPanel;
import com.google.gson.Gson;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.JMarsDarkTheme;
import mdlaf.themes.MaterialLiteTheme;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author bangmaple
 */
public final class CoronaFrame extends javax.swing.JFrame {

    private static Integer RELOAD_DATA_TIME = Integer.MAX_VALUE;
    private static String BASE_RESOURCE = "Resources";
    private static Boolean THEME = Boolean.FALSE;
    private ScheduledExecutorService service;
    private DefaultTableModel tblModel;
    private CoronaDAO coronaList;
    private String search = "";
    private final Locale currentLocale;
    private final ResourceBundle resource;

    public CoronaFrame() {
        currentLocale = new Locale("en", "US");
        resource = ResourceBundle.getBundle(BASE_RESOURCE, currentLocale);
        initializeMain();
    }

    public CoronaFrame(String language, String country, boolean theme) {
        currentLocale = new Locale(language, country);
        resource = ResourceBundle.getBundle(BASE_RESOURCE, currentLocale);
        THEME = theme;
        try {
            if (theme) {
                javax.swing.UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialLiteTheme()));
            } else {
                javax.swing.UIManager.setLookAndFeel(new MaterialLookAndFeel(new JMarsDarkTheme()));
            }
        } catch (UnsupportedLookAndFeelException e) {
            System.exit(0);
        }
        initializeMain();
    }

    private void initializeMain() {
        this.setUndecorated(true);
        this.setLayout(null);

        initComponents();
        modifyTableHeader();
        this.setLocationRelativeTo(null);
        loadLogo();
        tblModel = (DefaultTableModel) jTable1.getModel();
        setTableColumnSize();
        jTable1.setDefaultEditor(Object.class, null);
        coronaList = new CoronaDAO();
        init();
        txtMainConfirmed.setEditable(false);
        txtMainDeath.setEditable(false);
        txtMainRecovered.setEditable(false);
        cbTheme.addActionListener(((e) -> {
            String mode = String.valueOf(cbTheme.getModel().getSelectedItem());
            String[] localeStr = String.valueOf(cbLang.getModel().getSelectedItem()).split("-");
            if (mode.contains("ngày") || mode.contains("Light")) {
                restart();
                new CoronaFrame(localeStr[0], localeStr[1], true).setVisible(true);
            } else {
                restart();
                new CoronaFrame(localeStr[0], localeStr[1], false).setVisible(true);
            }

        }));
        cbLang.addActionListener((ActionEvent e) -> {
            String[] localeStr = String.valueOf(cbLang.getModel().getSelectedItem()).split("-");
            restart();
            new CoronaFrame(localeStr[0], localeStr[1], THEME).setVisible(true);
        });
    }

    private void restart() {
        this.setVisible(false);
        this.dispose();
    }

    private void initData() {
        try {
            getData();
            parseData();
        } catch (Exception e) {
        }
    }

    private void parseData(final List<Corona> list) {
        tblModel.setRowCount(0);
        for (int i = 0; i < list.size(); i++) {
            tblModel.addRow(new Object[]{i, list.get(i).getCode(), list.get(i).getName(),
                list.get(i).getLatest_data().getConfirmed(),
                list.get(i).getLatest_data().getRecovered(),
                list.get(i).getLatest_data().getDeaths()});
        }
        dateTimeLastUpdated();
    }

    private void getData() throws Exception {
        coronaList.clear();
        JSONArray jsonArray = (JSONArray) ((JSONObject) new JSONParser().parse(CoronaClientAPI.getJSON())).get("data");
        if (jsonArray != null) {
            Gson gson = new Gson();
            for (int i = 0; i < jsonArray.size(); i++) {
                coronaList.add(gson.fromJson(((JSONObject) jsonArray.get(i)).toJSONString(), Corona.class));
            }
            coronaList.sort();
        }
    }

    private void parseData() throws Exception {
        tblModel.setRowCount(0);
        Integer confirmed = 0, yesterday_confirmed = 0, recovered = 0, death = 0, yesterday_death = 0;
        List<Corona> list = coronaList.get();
        for (int i = 0; i < list.size(); i++) {
            tblModel.addRow(new Object[]{i, list.get(i).getCode(), list.get(i).getName(),
                list.get(i).getLatest_data().getConfirmed(),
                list.get(i).getLatest_data().getRecovered(),
                list.get(i).getLatest_data().getDeaths()});
            confirmed += list.get(i).getLatest_data().getConfirmed();
            recovered += list.get(i).getLatest_data().getRecovered();
            death += list.get(i).getLatest_data().getDeaths();
            yesterday_confirmed += list.get(i).getToday().getConfirmed();
            yesterday_death += list.get(i).getToday().getDeaths();
        }
        txtYesterdayConfirmed.setText("+" + yesterday_confirmed);
        txtYesterdayDeath.setText("+" + yesterday_death);

        txtMainConfirmed.setText(confirmed.toString());
        txtMainRecovered.setText(recovered.toString());
        txtMainDeath.setText(death.toString());
        txtSearch.setEditable(true);
        btnForceUpdate.setEnabled(true);
        btnSaveTime.setEnabled(true);
        btnSortASC.setEnabled(true);
        btnSortDSC.setEnabled(true);
        btnExport.setEnabled(true);
        dateTimeLastUpdated();

    }

    private void dateTimeLastUpdated() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(coronaList.get().get(0).getUpdated_at());
        if (String.valueOf(cbLang.getModel().getSelectedItem()).equals("en-US")) {
            txtLastUpdated.setText(resource.getString("txtLastUpdated") + new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(Date.from(Instant.from(accessor))));
        } else {
            txtLastUpdated.setText(resource.getString("txtLastUpdated") + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date.from(Instant.from(accessor))));
        }
    }

    private void init() throws ArrayIndexOutOfBoundsException {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new BackgroundJob(), 0, RELOAD_DATA_TIME, TimeUnit.SECONDS);
    }

    private void modifyTableHeader() {
        ((DefaultTableCellRenderer) jTable1.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        //header.setDefaultRenderer(new HeaderRenderer(jTable1));
        int[] alignments = new int[]{JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT};
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            jTable1.getTableHeader().getColumnModel().getColumn(i)
                    .setHeaderRenderer(new HeaderRenderer(jTable1, alignments[i]));
        }
    }

    private void loadLogo() {
        Icon logoIcon = new ImageIcon(new ImageIcon(getClass()
                .getResource("/logo.png")).getImage()
                .getScaledInstance(250, 45, java.awt.Image.SCALE_SMOOTH));
        lblLogo.setIcon(logoIcon);
        Icon bgIcon = new ImageIcon(new ImageIcon(getClass()
                .getResource("/background.png")).getImage()
                .getScaledInstance(80, 50, java.awt.Image.SCALE_SMOOTH));
        lblBackground.setIcon(bgIcon);
        Icon closeIcon = new ImageIcon(new ImageIcon(getClass()
                .getResource("/icons8_multiply_18px_1.png")).getImage()
                .getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH));
        lblClose.setIcon(closeIcon);

        Icon minMaxIcon = new ImageIcon(new ImageIcon(getClass()
                .getResource("/icons8_rectangle_stroked_18px.png")).getImage()
                .getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH));
        lblMaxMin.setIcon(minMaxIcon);

        Icon minIcon = new ImageIcon(new ImageIcon(getClass()
                .getResource("/icons8_minus_18px_1.png")).getImage()
                .getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH));
        lblMin.setIcon(minIcon);
    }

    private void setTableColumnSize() {
        int[] columnsWidth = {40, 40, 350, 105, 105};
        int i = 0;
        for (int columnWidthSize : columnsWidth) {
            TableColumn column = jTable1.getColumnModel().getColumn(i++);
            column.setMinWidth(columnWidthSize);
            column.setMaxWidth(columnWidthSize);
            column.setPreferredWidth(columnWidthSize);
        }
    }

    private static class HeaderRenderer implements TableCellRenderer {

        DefaultTableCellRenderer renderer;
        int horAlignment;

        public HeaderRenderer(JTable table, int horizontalAlignment) {
            horAlignment = horizontalAlignment;
            renderer = (DefaultTableCellRenderer) table.getTableHeader()
                    .getDefaultRenderer();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = renderer.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, col);
            JLabel label = (JLabel) c;
            label.setHorizontalAlignment(horAlignment);
            return label;
        }
    }

    private class BackgroundJob implements Runnable {

        @Override
        public void run() {
            try {
                getData();
                parseData();
            } catch (Exception e) {

            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new MotionPanel(this);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable(){
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            {
                renderer.setHorizontalAlignment(SwingConstants.LEFT);
            }

            @Override
            public TableCellRenderer getCellRenderer(int arg0, int arg1) {
                return renderer;
            }
        };
        txtMainConfirmed = new javax.swing.JTextField();
        txtMainRecovered = new javax.swing.JTextField();
        txtMainDeath = new javax.swing.JTextField();
        txtYesterdayConfirmed = new javax.swing.JLabel();
        txtYesterdayDeath = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblMin = new javax.swing.JLabel();
        lblMaxMin = new javax.swing.JLabel();
        lblClose = new javax.swing.JLabel();
        txtConfirmed = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        txtRecovered = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        txtDeath = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        txtCountry = new javax.swing.JLabel();
        txtYesterdayConfirmed1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtYesterdayDeath1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new MotionPanel(this);
        jLabel5 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSortASC = new javax.swing.JButton();
        btnSortDSC = new javax.swing.JButton();
        cbSort = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtRecoveryRate = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtPopulation = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtCasePerMillionPopulation = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtRecoveredVsDeathRatio = new javax.swing.JTextField();
        lblBackground = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();
        txtCountry1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtLastUpdated = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnForceUpdate = new javax.swing.JButton();
        btnSaveTime = new javax.swing.JButton();
        cbTime = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        txtLocation = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        cbLang = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        cbTheme = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"...", "...", resource.getString("tblLabelWaitingData"), resource.getString("tblLabelWaiting"), "...", "..."}
            },
            new String [] {
                "", "", resource.getString("txtCountryName"), resource.getString("txtConfirmed"), resource.getString("txtRecovered"), resource.getString("txtDeaths")
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        txtMainConfirmed.setEditable(false);
        txtMainConfirmed.setBackground(new java.awt.Color(255, 204, 204));
        txtMainConfirmed.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtMainConfirmed.setForeground(new java.awt.Color(204, 0, 0));
        txtMainConfirmed.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        txtMainRecovered.setEditable(false);
        txtMainRecovered.setBackground(new java.awt.Color(204, 255, 204));
        txtMainRecovered.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtMainRecovered.setForeground(new java.awt.Color(0, 153, 0));
        txtMainRecovered.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtMainRecovered.setSize(new java.awt.Dimension(84, 24));

        txtMainDeath.setEditable(false);
        txtMainDeath.setBackground(new java.awt.Color(204, 204, 204));
        txtMainDeath.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtMainDeath.setForeground(new java.awt.Color(102, 102, 102));
        txtMainDeath.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        txtYesterdayConfirmed.setForeground(new java.awt.Color(255, 0, 0));
        txtYesterdayConfirmed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        txtYesterdayDeath.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(255, 153, 153));
        jTextField1.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(255, 0, 51));
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText(resource.getString("txtConfirmed"));

        jTextField2.setEditable(false);
        jTextField2.setBackground(new java.awt.Color(204, 255, 204));
        jTextField2.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField2.setForeground(new java.awt.Color(0, 153, 0));
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField2.setText(resource.getString("txtRecovered"));

        jTextField3.setEditable(false);
        jTextField3.setBackground(new java.awt.Color(153, 153, 153));
        jTextField3.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField3.setForeground(new java.awt.Color(51, 51, 51));
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField3.setText(resource.getString("txtDeaths"));

        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(resource.getString("txtSinceYesterday"));
        jLabel3.setToolTipText("");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(resource.getString("txtSinceYesterday"));
        jLabel4.setToolTipText("");

        lblMin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMinMouseClicked(evt);
            }
        });

        lblMaxMin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMaxMinMouseClicked(evt);
            }
        });

        lblClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCloseMouseClicked(evt);
            }
        });

        txtConfirmed.setEditable(false);
        txtConfirmed.setBackground(new java.awt.Color(255, 204, 204));
        txtConfirmed.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtConfirmed.setForeground(new java.awt.Color(204, 0, 0));
        txtConfirmed.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTextField4.setEditable(false);
        jTextField4.setBackground(new java.awt.Color(255, 153, 153));
        jTextField4.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField4.setForeground(new java.awt.Color(255, 0, 51));
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setText(resource.getString("txtConfirmed"));

        txtRecovered.setEditable(false);
        txtRecovered.setBackground(new java.awt.Color(204, 255, 204));
        txtRecovered.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtRecovered.setForeground(new java.awt.Color(0, 153, 0));
        txtRecovered.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtRecovered.setSize(new java.awt.Dimension(84, 24));

        jTextField5.setEditable(false);
        jTextField5.setBackground(new java.awt.Color(204, 255, 204));
        jTextField5.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField5.setForeground(new java.awt.Color(0, 153, 0));
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setText(resource.getString("txtRecovered"));

        txtDeath.setEditable(false);
        txtDeath.setBackground(new java.awt.Color(204, 204, 204));
        txtDeath.setFont(new java.awt.Font("Lucida Grande", 1, 20)); // NOI18N
        txtDeath.setForeground(new java.awt.Color(102, 102, 102));
        txtDeath.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTextField6.setEditable(false);
        jTextField6.setBackground(new java.awt.Color(153, 153, 153));
        jTextField6.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jTextField6.setForeground(new java.awt.Color(51, 51, 51));
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField6.setText(resource.getString("txtDeaths"));

        txtCountry.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        txtCountry.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtCountry.setText(resource.getString("txtSelectCountry"));

        txtYesterdayConfirmed1.setForeground(new java.awt.Color(255, 0, 0));
        txtYesterdayConfirmed1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtYesterdayConfirmed1.setText("+0");
        txtYesterdayConfirmed1.setToolTipText("");

        jLabel6.setForeground(new java.awt.Color(255, 0, 0));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(resource.getString("txtSinceYesterday"));
        jLabel6.setToolTipText("");

        txtYesterdayDeath1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtYesterdayDeath1.setText("+0");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(resource.getString("txtSinceYesterday"));
        jLabel7.setToolTipText("");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resource.getString("txtSearch")));

        jLabel5.setText(resource.getString("txtPartOfCountryName"));

        txtSearch.setEditable(false);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        btnSortASC.setText(resource.getString("txtSortASC"));
        btnSortASC.setEnabled(false);
        btnSortASC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSortASCActionPerformed(evt);
            }
        });

        btnSortDSC.setText(resource.getString("txtSortDSC"));
        btnSortDSC.setEnabled(false);
        btnSortDSC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSortDSCActionPerformed(evt);
            }
        });

        cbSort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { resource.getString("txtConfirmedSort"), resource.getString("txtRecoveredSort"), resource.getString("txtDeathsSort") }));

        jLabel8.setText(resource.getString("txtSortBy"));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSearch)
                            .addComponent(cbSort, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnSortASC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 167, Short.MAX_VALUE)
                        .addComponent(btnSortDSC)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSortASC)
                    .addComponent(btnSortDSC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resource.getString("txtStatistics")));

        jLabel9.setText(resource.getString("txtRecoveryDeathRate"));

        txtRecoveryRate.setEditable(false);

        jLabel10.setText(resource.getString("txtPopulation"));

        txtPopulation.setEditable(false);

        jLabel11.setText(resource.getString("txtCasesPerMillion"));

        txtCasePerMillionPopulation.setEditable(false);

        jLabel12.setText(resource.getString("txtRecoveredDeathRatio"));

        txtRecoveredVsDeathRatio.setEditable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel10))
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtRecoveredVsDeathRatio, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                    .addComponent(txtCasePerMillionPopulation)
                    .addComponent(txtRecoveryRate, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPopulation))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtPopulation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtRecoveryRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtCasePerMillionPopulation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtRecoveredVsDeathRatio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        lblBackground.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblBackgroundMouseClicked(evt);
            }
        });

        lblLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLogoMouseClicked(evt);
            }
        });

        txtCountry1.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        txtCountry1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtCountry1.setText(resource.getString("txtCurrentWorldWide"));

        txtLastUpdated.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        txtLastUpdated.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txtLastUpdated.setText(resource.getString("txtLastUpdated"));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resource.getString("txtStatus")));

        jLabel1.setText(resource.getString("txtTimeInterval"));

        jLabel2.setText(resource.getString("txtSecond"));

        btnForceUpdate.setText(resource.getString("txtForceUpdate"));
        btnForceUpdate.setEnabled(false);
        btnForceUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnForceUpdateActionPerformed(evt);
            }
        });

        btnSaveTime.setText(resource.getString("txtSaveAndStart"));
        btnSaveTime.setEnabled(false);
        btnSaveTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveTimeActionPerformed(evt);
            }
        });

        cbTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5", "30", "60", "180", "300", "900", "1800", "3600", "10800", "21600", "43200", "86400", " " }));
        cbTime.setSelectedIndex(2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(cbTime, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(btnSaveTime, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(btnForceUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(btnForceUpdate)
                    .addComponent(btnSaveTime)
                    .addComponent(cbTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resource.getString("txtExport")));

        jLabel13.setText(resource.getString("txtLocation"));

        btnExport.setText(resource.getString("txtExportToCSV"));
        btnExport.setEnabled(false);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnExport)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExport))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("<html>\n[BangMaple]<br/>\nCorona Tracker<br/>\n<br/>\n"+resource.getString("txtVersion")+": <b>1.0</b>\n</html>");

        cbLang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { resource.getString("langMain"), resource.getString("langSub") }));

        jLabel15.setText(resource.getString("txtLanguage"));

        jLabel16.setText(resource.getString("txtTheme"));

        cbTheme.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { resource.getString("txtDarkMode"), resource.getString("txtLightMode") }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtLastUpdated, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtCountry1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(lblMin, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMaxMin, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16)
                                    .addComponent(cbTheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtMainConfirmed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtYesterdayConfirmed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(14, 14, 14)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtMainRecovered)
                                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(15, 15, 15)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtMainDeath)
                                            .addComponent(jTextField3)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtYesterdayDeath, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jSeparator1)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtConfirmed, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(14, 14, 14)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtRecovered)
                                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(15, 15, 15)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtDeath)
                                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtYesterdayConfirmed1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtYesterdayDeath1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addComponent(txtCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap(15, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMaxMin, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCountry1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(txtLastUpdated, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtMainConfirmed)
                                    .addComponent(txtMainRecovered)
                                    .addComponent(txtMainDeath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtYesterdayConfirmed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtYesterdayDeath, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, 0)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(txtCountry)
                                .addGap(5, 5, 5)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtConfirmed)
                                    .addComponent(txtRecovered)
                                    .addComponent(txtDeath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtYesterdayConfirmed1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtYesterdayDeath1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, 0)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbTheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblMinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMinMouseClicked
        // TODO add your handling code here:
        setExtendedState(JFrame.ICONIFIED);
    }//GEN-LAST:event_lblMinMouseClicked

    private void lblMaxMinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMaxMinMouseClicked
        // TODO add your handling code here:
//        if (!flagMaxMinimize) {
//            setExtendedState(JFrame.MAXIMIZED_BOTH);
//            flagMaxMinimize = true;
//        } else {
//            setExtendedState(JFrame.NORMAL);
//            flagMaxMinimize = false;
//        }
    }//GEN-LAST:event_lblMaxMinMouseClicked

    private void lblCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCloseMouseClicked
        // TODO add your handling code here:

        System.exit(0);
    }//GEN-LAST:event_lblCloseMouseClicked

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        int pos = jTable1.getSelectedRow();
        String code = String.valueOf(tblModel.getValueAt(pos, 1));
        try {
            Corona c = coronaList.getEntity(code);
            txtCountry.setText(c.getName());
            txtConfirmed.setText(String.valueOf(c.getLatest_data().getConfirmed()));
            txtRecovered.setText(String.valueOf(c.getLatest_data().getRecovered()));
            txtDeath.setText(String.valueOf(c.getLatest_data().getDeaths()));
            txtYesterdayConfirmed1.setText("+" + c.getToday().getConfirmed());
            txtYesterdayDeath1.setText("+" + c.getToday().getDeaths());
            txtPopulation.setText(String.valueOf(c.getPopulation()));
            if (c.getLatest_data().getCalculated().getRecovery_rate() != null) {
                txtRecoveryRate.setText(String.format("%.2f", c.getLatest_data().getCalculated().getRecovery_rate()) + " %");
            } else {
                txtRecoveryRate.setText(resource.getString("txtNoData"));
            }
            if (c.getLatest_data().getCalculated().getDeath_rate() != null) {
                txtRecoveryRate.setText(txtRecoveryRate.getText() + " - " + String.format("%.2f", c.getLatest_data().getCalculated().getDeath_rate()) + " %");
            } else {
                txtRecoveryRate.setText(txtRecoveryRate.getText() + " - " + resource.getString("txtNoData"));
            }
            txtCasePerMillionPopulation.setText(String.format("%.2f", c.getLatest_data().getCalculated().getCases_per_million_population()));
            if (c.getLatest_data().getCalculated().getRecovered_vs_death_ratio() == null) {
                txtRecoveredVsDeathRatio.setText(resource.getString("txtNoDataAvailable"));
            } else {
                txtRecoveredVsDeathRatio.setText(String.format("%.2f", c.getLatest_data().getCalculated().getRecovered_vs_death_ratio()) + "%");
            }
        } catch (Exception e) {

        }

    }//GEN-LAST:event_jTable1MouseClicked

    private void lblBackgroundMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblBackgroundMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_lblBackgroundMouseClicked

    private void lblLogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogoMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_lblLogoMouseClicked

    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() >= 65 && evt.getKeyCode() <= 90) {
            search += String.valueOf(evt.getKeyChar());
        }
        if (!search.equals("")) {
            if (evt.getKeyCode() == 8) {
                search = search.substring(0, search.length() - 1);
            }
        }
        List<Corona> list = coronaList.getCoronaByLikeCountryName(search.trim().toUpperCase());
        parseData(list);

    }//GEN-LAST:event_txtSearchKeyPressed

    private void btnForceUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnForceUpdateActionPerformed
        // TODO add your handling code here:
        tblModel.setRowCount(0);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                initData();
                return null;
            }
        }.execute();
    }//GEN-LAST:event_btnForceUpdateActionPerformed

    private void btnSaveTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveTimeActionPerformed
        // TODO add your handling code here:

        RELOAD_DATA_TIME = Integer.parseInt(String.valueOf(cbTime.getSelectedItem()));
        JOptionPane.showMessageDialog(this, resource.getString("txtSuccessfullySetTimeInterval"));
        if (!service.isShutdown()) {
            service.shutdownNow();
        }
        init();
    }//GEN-LAST:event_btnSaveTimeActionPerformed

    private void btnSortASCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSortASCActionPerformed
        // TODO add your handling code here:
        String sortBy = String.valueOf(cbSort.getModel().getSelectedItem());
        List<Corona> list = coronaList.get();
        if (sortBy.contains("bệnh") || sortBy.contains("Confirmed")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getConfirmed() < o2.getLatest_data().getConfirmed()) {
                    return -1;
                } else if (o1.getLatest_data().getConfirmed() > o2.getLatest_data().getConfirmed()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        } else if (sortBy.contains("hồi") || sortBy.contains("Recovered")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getRecovered() < o2.getLatest_data().getRecovered()) {
                    return -1;
                } else if (o1.getLatest_data().getRecovered() > o2.getLatest_data().getRecovered()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        } else if (sortBy.contains("vong") || sortBy.contains("Deaths")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getDeaths() < o2.getLatest_data().getDeaths()) {
                    return -1;
                } else if (o1.getLatest_data().getDeaths() > o2.getLatest_data().getDeaths()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        }
    }//GEN-LAST:event_btnSortASCActionPerformed

    private void btnSortDSCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSortDSCActionPerformed
        // TODO add your handling code here:
        String sortBy = String.valueOf(cbSort.getModel().getSelectedItem());
        List<Corona> list = coronaList.get();
        if (sortBy.contains("bệnh") || sortBy.contains("Confirmed")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getConfirmed() > o2.getLatest_data().getConfirmed()) {
                    return -1;
                } else if (o1.getLatest_data().getConfirmed() < o2.getLatest_data().getConfirmed()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        } else if (sortBy.contains("hồi") || sortBy.contains("Recovered")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getRecovered() > o2.getLatest_data().getRecovered()) {
                    return -1;
                } else if (o1.getLatest_data().getRecovered() < o2.getLatest_data().getRecovered()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        } else if (sortBy.contains("vong") || sortBy.contains("Deaths")) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getLatest_data().getDeaths() > o2.getLatest_data().getDeaths()) {
                    return -1;
                } else if (o1.getLatest_data().getDeaths() < o2.getLatest_data().getDeaths()) {
                    return 1;
                }
                return 0;
            });
            parseData(coronaList.get());
        }


    }//GEN-LAST:event_btnSortDSCActionPerformed
    private class MyFileChooser extends JFileChooser {

        @Override
        public JDialog createDialog(Component parent) throws HeadlessException {
            return super.createDialog(parent);
        }
    }

    private void initSaveDialog() {
        MyFileChooser chooser = new MyFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setSelectedFile(new File("myfile.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter(".csv (Excel Comma-separated values)", "csv"));
        final JDialog dialog = chooser.createDialog(null);
        chooser.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser chooser = (JFileChooser) evt.getSource();
                if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
                    String filename = chooser.getSelectedFile().getAbsolutePath();
                    if (!filename.endsWith(".csv")) {
                        filename += ".csv";
                    }
                    txtLocation.setText(filename);
                    dialog.setVisible(false);
                } else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
                    dialog.setVisible(false);
                }
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.setVisible(false);
            }
        });

        dialog.setVisible(true);
    }

    private void initExportToCSV() {
        List<Corona> list = coronaList.get();
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(txtLocation.getText());
            bw = new BufferedWriter(fw);
            bw.write("Country Code,Country Name,Population,Confirmed,Recovered,Deaths,"
                    + "Critical,Recovery Rate,Cases Per Million,Death Rate,Recovered vs"
                    + " Dead Ratio,Updated At,Latitude,Longitude");
            bw.newLine();
            for (int i = 0; i < list.size(); i++) {
                bw.write(list.get(i).toString());
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, resource.getString("txtSuccessfullyExported") + txtLocation.getText());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, resource.getString("txtErrorExporting"));
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        initSaveDialog();
        initExportToCSV();

    }//GEN-LAST:event_btnExportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(new MaterialLookAndFeel(new JMarsDarkTheme()));
        } catch (UnsupportedLookAndFeelException e) {
            System.exit(0);
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new CoronaFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnForceUpdate;
    private javax.swing.JButton btnSaveTime;
    private javax.swing.JButton btnSortASC;
    private javax.swing.JButton btnSortDSC;
    private javax.swing.JComboBox<String> cbLang;
    private javax.swing.JComboBox<String> cbSort;
    private javax.swing.JComboBox<String> cbTheme;
    private javax.swing.JComboBox<String> cbTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JLabel lblClose;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMaxMin;
    private javax.swing.JLabel lblMin;
    private javax.swing.JTextField txtCasePerMillionPopulation;
    private javax.swing.JTextField txtConfirmed;
    private javax.swing.JLabel txtCountry;
    private javax.swing.JLabel txtCountry1;
    private javax.swing.JTextField txtDeath;
    private javax.swing.JLabel txtLastUpdated;
    private javax.swing.JTextField txtLocation;
    private javax.swing.JTextField txtMainConfirmed;
    private javax.swing.JTextField txtMainDeath;
    private javax.swing.JTextField txtMainRecovered;
    private javax.swing.JTextField txtPopulation;
    private javax.swing.JTextField txtRecovered;
    private javax.swing.JTextField txtRecoveredVsDeathRatio;
    private javax.swing.JTextField txtRecoveryRate;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JLabel txtYesterdayConfirmed;
    private javax.swing.JLabel txtYesterdayConfirmed1;
    private javax.swing.JLabel txtYesterdayDeath;
    private javax.swing.JLabel txtYesterdayDeath1;
    // End of variables declaration//GEN-END:variables
}
