package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.packet.IQ;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.os.TypedValue;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.vm.ManageBindings;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: marko
 * Date: Jul 17, 2009
 * Time: 11:57:06 PM
 */
public class ManageBindingsPanel extends JPanel implements ActionListener, TableModelListener {

    protected JTable bindingsTable;
    protected VmControlFrame vmControlFrame;
    protected int count = 0;
    protected static final String GET = "get";
    protected static final String SET = "set";
    protected static final String ADD = "add";
    protected static final String REMOVE = "remove";
    protected static final String NULL = "null";


    public ManageBindingsPanel(VmControlFrame vmControlFrame) {
        super(new BorderLayout());
        this.vmControlFrame = vmControlFrame;
        this.setOpaque(false);
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][]{}, new Object[]{LinkedProcess.NAME_ATTRIBUTE, LinkedProcess.VALUE_ATTRIBUTE, LinkedProcess.DATATYPE_ATTRIBUTE, "null"});
        tableModel.addTableModelListener(this);
        this.bindingsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(this.bindingsTable);
        this.bindingsTable.setFillsViewportHeight(true);
        this.bindingsTable.setRowHeight(20);
        this.bindingsTable.setFont(new Font(null, Font.PLAIN, 13));
        this.bindingsTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        this.bindingsTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        this.bindingsTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        this.bindingsTable.getColumnModel().getColumn(3).setPreferredWidth(30);

        JComboBox xmlSchemaBox = new JComboBox();
        for (VMBindings.XMLSchemaDatatype dataType : VMBindings.XMLSchemaDatatype.values()) {
            xmlSchemaBox.addItem(dataType.abbreviate());
        }
        JCheckBox nullCheckBox = new JCheckBox();
        nullCheckBox.setActionCommand(NULL);
        nullCheckBox.addActionListener(this);

        this.bindingsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(xmlSchemaBox));
        this.bindingsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(nullCheckBox));
        //this.bindingsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
        //this.bindingsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton getButton = new JButton(GET);
        JButton setButton = new JButton(SET);
        JButton addButton = new JButton(ImageHolder.addIcon);
        addButton.setActionCommand(ADD);
        JButton removeButton = new JButton(ImageHolder.removeIcon);
        removeButton.setActionCommand(REMOVE);
        leftButtonPanel.add(addButton);
        leftButtonPanel.add(removeButton);
        rightButtonPanel.add(getButton);
        rightButtonPanel.add(setButton);
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        setButton.addActionListener(this);
        getButton.addActionListener(this);
        leftButtonPanel.setOpaque(false);
        rightButtonPanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        scrollPane.setOpaque(false);

        JLabel helpLabel = new JLabel("Manage bindings by highlighting particular rows to set and get.");
        helpLabel.setOpaque(false);
        this.add(helpLabel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);


    }

    public void tableChanged(TableModelEvent event) {
        if (event.getType() == TableModelEvent.UPDATE || event.getType() == TableModelEvent.INSERT) {
            DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
            int row = event.getFirstRow();
            int column = event.getColumn();
            if (column == 0) {
                String editedName = (String) tableModel.getValueAt(row, column);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (i != row) {
                        if (tableModel.getValueAt(i, 0).equals(editedName)) {
                            tableModel.setValueAt("name_already_exists-" + this.count, row, column);
                        }
                    }
                }
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        if (event.getActionCommand().equals(ADD)) {
            tableModel.addRow(new Object[]{LinkedProcess.NAME_ATTRIBUTE + this.count, LinkedProcess.VALUE_ATTRIBUTE + this.count, "xsd:string", false});
            count++;
        } else if (event.getActionCommand().equals(REMOVE)) {
            if (this.bindingsTable.getSelectedRow() > -1) {
                int smallestRow = Integer.MAX_VALUE;
                int x = 0;
                for (int row : this.bindingsTable.getSelectedRows()) {
                    tableModel.removeRow(row - x);
                    if (row - x < smallestRow)
                        smallestRow = row - x;
                    x++;
                }
                if (smallestRow > 0 && smallestRow != Integer.MAX_VALUE)
                    this.bindingsTable.setRowSelectionInterval(smallestRow - 1, smallestRow - 1);
                else if (tableModel.getRowCount() > 0)
                    this.bindingsTable.setRowSelectionInterval(0, 0);
            }

        } else if (event.getActionCommand().equals(GET)) {
            ManageBindings manageBindings = this.getManageBindings(IQ.Type.GET);
            if (manageBindings != null)
                vmControlFrame.getVilleinGui().getXmppVillein().getConnection().sendPacket(manageBindings);
        } else if (event.getActionCommand().equals(SET)) {
            ManageBindings manageBindings = this.getManageBindings(IQ.Type.SET);
            if (manageBindings != null)
                vmControlFrame.getVilleinGui().getXmppVillein().getConnection().sendPacket(manageBindings);
        } else if (event.getActionCommand().equals(NULL)) {
            int row = this.bindingsTable.getSelectedRow();
            if ((Boolean) tableModel.getValueAt(row, 3)) {
                tableModel.setValueAt("", row, 1);
                tableModel.setValueAt("", row, 2);
                tableModel.setValueAt(true, row, 3);
            } else {
                tableModel.setValueAt(false, row, 3);
            }

        }
    }

    public ManageBindings getManageBindings(IQ.Type setOrGet) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setType(setOrGet);
        manageBindings.setTo(this.vmControlFrame.getVmStruct().getFullJid());
        manageBindings.setFrom(this.vmControlFrame.getVilleinGui().getXmppVillein().getFullJid());
        manageBindings.setVmPassword(this.vmControlFrame.getVmStruct().getVmPassword());
        for (int row : this.bindingsTable.getSelectedRows()) {
            String name = (String) tableModel.getValueAt(row, 0);
            String value = (String) tableModel.getValueAt(row, 1);
            boolean isNull = (Boolean) tableModel.getValueAt(row, 3);

            if (setOrGet == IQ.Type.SET)
                if ((tableModel.getValueAt(row, 2) == null || ((String) tableModel.getValueAt(row, 2)).length() == 0) && !isNull)
                    JOptionPane.showMessageDialog(null, "select a datatype for the binding", "datatype error", JOptionPane.ERROR_MESSAGE);
                else {
                    try {
                        if (!isNull) {
                            String datatype = VMBindings.XMLSchemaDatatype.expandDatatypeAbbreviation((String) tableModel.getValueAt(row, 2));
                            manageBindings.addBinding(name, value, datatype);
                        } else {
                            manageBindings.addBinding(name, null, null);
                        }
                    } catch (InvalidValueException e) {
                        JOptionPane.showMessageDialog(null, "illegal argument for the specified datatype", "illegal type conversion error", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                }
            else {
                try {
                    manageBindings.addBinding(name, null, null);
                } catch (InvalidValueException e) {
                    // This won't happen because this is a GET
                    System.exit(1);
                }
            }

        }
        return manageBindings;
    }

    public void handleIncomingManageBindings(ManageBindings manageBindings) {
        DefaultTableModel tableModel = (DefaultTableModel) this.bindingsTable.getModel();
        if (manageBindings.getType() == IQ.Type.RESULT) {
            for (String name : manageBindings.getBindings().keySet()) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(name)) {
                        TypedValue typedValue = manageBindings.getBinding(name);
                        if (null == typedValue) {
                            tableModel.setValueAt("", i, 1);
                            tableModel.setValueAt("", i, 2);
                            tableModel.setValueAt(true, i, 3);
                        } else {
                            tableModel.setValueAt(typedValue.getValue(), i, 1);
                            tableModel.setValueAt(typedValue.getDatatype().abbreviate(), i, 2);
                            tableModel.setValueAt(false, i, 3);
                        }

                    }
                }
            }
        }
    }

    /*public void paintComponent(Graphics g) {
        g.drawImage(ImageHolder.cowBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }*/
}
