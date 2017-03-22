package uk.co.raphel.railsim.configapp;/**
 * Created by johnr on 19/02/2017.
 */

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.co.raphel.railsim.common.TrackDiagramEntry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainFrame extends JFrame implements ActionListener, TableModelListener, ResourceLoaderAware {

    private final ServiceTableModel theTableModel = new ServiceTableModel();
    private ResourceLoader resourceLoader;

    private final List<TrackDiagramEntry> trackDiagram = new LinkedList<>();

    private final List<EditableTrainService> dataBase = new ArrayList<>();

    private int currentDataPointer;
    private final JTextField txtService = new JTextField("");
    private final JTextField txtStart = new JTextField("");
    private final JTextField txtClass = new JTextField("");
    private final JTextField txtEquipment = new JTextField("");
    private final JTextField txtDest = new JTextField("");
    private final JPanel buttonPanel = new JPanel();

    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1200, 800));


        loadData();

        theTableModel.addTableModelListener(this);
       JTable theTable = new JTable(theTableModel);
        JScrollPane scrollPane = new JScrollPane(theTable);
        theTable.setFillsViewportHeight(true);
        scrollPane.setPreferredSize(new Dimension(600, 800));
         GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.setSize(600,800);
        JButton nextButton = new JButton(">>");  nextButton.setActionCommand("NEXT"); nextButton.addActionListener(this);
        JButton prevButton = new JButton("<<"); prevButton.setActionCommand("PREV"); prevButton.addActionListener(this);
        JButton saveButton = new JButton("Save")  ; saveButton.setActionCommand("SAVE"); saveButton.addActionListener(this);
        JButton exitButton = new JButton("Exit"); exitButton.setActionCommand("EXIT"); exitButton.addActionListener(this);
        buttonPanel.setBorder(new TitledBorder("Buttons"));
        buttonPanel.setLayout(new GridBagLayout());
        gridbag.setConstraints(buttonPanel,c);
        c.gridx = 0; c.gridy = 0; buttonPanel.add(nextButton,c);
        c.gridx = 1; c.gridy = 0; buttonPanel.add(prevButton);
        c.gridx = 2; c.gridy = 0; buttonPanel.add(saveButton);
        c.gridx = 3; c.gridy = 0; buttonPanel.add(exitButton);
        JLabel lblService = new JLabel("Service");
        JLabel lblStart = new JLabel("Start");
        JLabel lblClass = new JLabel("Class");
        JLabel lblEquipment = new JLabel("Engine");
        JLabel lblDest = new JLabel("Destination");
        c.gridx = 0; c.gridy = 1; c.gridwidth=1; buttonPanel.add(lblService,c); c.gridx = 1; c.gridy = 1; c.gridwidth = 3;  buttonPanel.add(txtService,c);
        c.gridx = 0; c.gridy = 2;  c.gridwidth=1; buttonPanel.add(lblStart,c); c.gridx = 1; c.gridy = 2;c.gridwidth = 3;  buttonPanel.add(txtStart,c);
        c.gridx = 0; c.gridy = 3; c.gridwidth=1;  buttonPanel.add(lblClass,c); c.gridx = 1; c.gridy = 3; c.gridwidth = 3; buttonPanel.add(txtClass,c);
        c.gridx = 0; c.gridy = 4; c.gridwidth=1;  buttonPanel.add(lblEquipment,c); c.gridx = 1; c.gridy = 4;c.gridwidth = 3;  buttonPanel.add(txtEquipment,c);
        c.gridx = 0; c.gridy = 5; c.gridwidth=1;  buttonPanel.add(lblDest,c); c.gridx = 1; c.gridy = 5; c.gridwidth = 3;  buttonPanel.add(txtDest,c);

        add(buttonPanel);


        pack();

        setVisible(true);
        setState(Frame.NORMAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("NEXT")) {
            updateServiceHeader();
            if(currentDataPointer < dataBase.size()-1) {
                currentDataPointer++;
                setCurrentDataToTableModel();
            }
        }
        if(e.getActionCommand().equals("PREV")) {
             updateServiceHeader()   ;
             if(currentDataPointer > 0) {
                 currentDataPointer--;
                 setCurrentDataToTableModel();
             }
        }
        if(e.getActionCommand().equals("SAVE")) {
            updateServiceHeader();
            saveOutput();
        }
        if(e.getActionCommand().equals("EXIT")) {
            this.dispose();
            System.exit(0);
        }

    }

    private void updateServiceHeader() {
        EditableTrainService srv = dataBase.get(currentDataPointer);
        srv.setStartTime(this.txtService.getText());
        srv.setStart(this.txtStart.getText());
        srv.setDestination(this.txtDest.getText());
        srv.setEngine(this.txtEquipment.getText());
        srv.setServiceClass(txtClass.getText());
    }
    private void loadData() {
        theTableModel.clear();
        loadTrackMap(getResource("classpath:TrackMapDown.csv"));


        theTableModel.setTrackNames(trackDiagram);
        loadServices(getResource("classpath:ServicesDown1.csv"));
        currentDataPointer =0;
        setCurrentDataToTableModel();
    }


    private void saveOutput() {
        String outfileName = "c:\\temp.csv";

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outfileName))) {

            // Write header
            writer.write("Train,From,Class,Engine,Destination");

            // Write list of tracksections
            trackDiagram.stream().forEach(t -> {
                try {
                    writer.write("," + t.getId());
                }   catch(IOException e) {

                }
            });
            writer.write("\r\n");

            // Write each service
            dataBase.stream().forEach( srv -> {
                try {
                    writer.write(srv.getStartTime() + "," + srv.getStart() + "," + srv.getServiceClass() + "," +
                            srv.getEngine() + "," + srv.getDestination());
                    trackDiagram.stream().forEach(dia -> {
                        try {
                            if(srv.getCallingPoints().containsKey(dia.getId())) {

                                writer.write("," + srv.getCallingPoints().get(dia.getId()));
                            } else {
                                writer.write(",");
                            }
                        }  catch(IOException ioe) {

                        }
                    });
                    writer.write("\r\n");
                } catch(IOException ioe) {

                }
            });
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setCurrentDataToTableModel() {
        EditableTrainService srv = dataBase.get(currentDataPointer);
        theTableModel.setData(srv);
        txtService.setText(srv.getStartTime());
        txtStart.setText(srv.getStart());
        txtClass.setText(srv.getServiceClass());
        txtEquipment.setText(srv.getEngine());
        txtDest.setText(srv.getDestination());
        buttonPanel.invalidate();

    }
    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if(column >=0) {
            TableModel model = (TableModel) e.getSource();
            String columnName = model.getColumnName(column);
            Object data = model.getValueAt(row, column);

            EditableTrainService toChange = dataBase.get(currentDataPointer);
            toChange.getCallingPoints().put(trackDiagram.get(row).getId(),(String)data);
        }
    }
    private void loadTrackMap(Resource trackMap) {
        try{
            //InputStream is = trackMap.getInputStream();
            File inFile = trackMap.getFile()   ;
            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line = br.readLine(); // SKIP HEADER
            while ((line = br.readLine()) != null) {
                TrackDiagramEntry diagramEntry = new TrackDiagramEntry(line);
                //System.out.println(diagramEntry.getId() + ":" + diagramEntry.getName());
                trackDiagram.add(diagramEntry);

            }
            br.close();


        }catch(IOException e){
            e.printStackTrace();
        }

    }

    private void loadServices(Resource resource ){

        int trainId = 1;

        try{
            //InputStream is = resource.getInputStream();
            File inFile = resource.getFile()   ;

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            // Read the header line and index the track sections
            String headerLine = br.readLine();
            List<Integer> indexList = new LinkedList<>();
           // System.out.println(headerLine);
            if(headerLine != null && headerLine.length() >0) {
                String indexes[] = headerLine.split(",");
                for(int i= 5; i<indexes.length; i++) {
                    int trackSection = Integer.parseInt(indexes[i]);
                    indexList.add(trackSection);
                }
            }
            while ((line = br.readLine()) != null) {
                // Only read lines with service defined (may be being built still!)
                if(line.split(",").length >=5) {

                   dataBase.add(new EditableTrainService(line, indexList));

                }

            }
            br.close();


        }catch(IOException e) {
            e.printStackTrace();
        }

    }



    public void setResourceLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }

    private Resource getResource(String location){
        return resourceLoader.getResource(location);
    }

}