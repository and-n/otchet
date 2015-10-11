package ru.rzd.otchet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ATonevitskiy
 */
public class Form extends javax.swing.JFrame {

    /**
     * Проверка на запуск в консоли или графически
     */
    public static boolean ISCONSOLE = false;

    /**
     * Creates new form Form
     */
    public Form() {
        if (!ISCONSOLE) {
            initComponents();
        }
    }

    private static Logic logic = new Logic();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        dateChooserCombo1 = new datechooser.beans.DateChooserCombo();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Отчеты ЕСПП");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusCycleRoot(false);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton1.setText("Справка по итогам суток");
        jButton1.setToolTipText("");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jMenu1.setText("Меню");

        jMenuItem1.setText("Выход");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Справка");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addComponent(dateChooserCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(47, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(dateChooserCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(119, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        System.out.println("start");
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(1);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            Calendar date = dateChooserCombo1.getSelectedDate();
            logic.createReport(date, this);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при запросе из базы.");
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(rootPane, "Невозможно сохранить файл!", "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, "Невозможно сохранить файл!", "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
            }
            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Form().setVisible(true);
                }
            }
            );
        } else if ("console".equals(args[0].toLowerCase())) {
            runConsole(args);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private datechooser.beans.DateChooserCombo dateChooserCombo1;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    // End of variables declaration//GEN-END:variables

    public String selectSaveFile() {
        JFileChooser c = new JFileChooser();
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int rVal = c.showSaveDialog(this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            return c.getSelectedFile().getAbsolutePath();
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
            return "";
        }
        return selectSaveFile();
    }

    private static void runConsole(String[] args) {
        System.out.println("CONSOLE start");
        ISCONSOLE = true;
        if (args.length == 1) {
            try {
                logic.createReport(Calendar.getInstance(), null);
            } catch (SQLException | IOException ex) {
                Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (args.length == 3) {
            DateFormat df1 = new SimpleDateFormat("dd.MM.yyyy");
            DateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
            Date start = null, end = null;
            try {
                start = df1.parse(args[1]);
                end = df1.parse(args[2]);
            } catch (ParseException ex) {
                try {
                    start = df2.parse(args[1]);
                    end = df2.parse(args[2]);
                } catch (ParseException ex1) {
                    Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            if (start != null && end != null) {
                for (int d = start.getDate(); start.before(end) || start.equals(end); start.setDate(++d)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(start);
                    try {
                        logic.createReport(c, null);
                    } catch (SQLException ex) {
                        Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
