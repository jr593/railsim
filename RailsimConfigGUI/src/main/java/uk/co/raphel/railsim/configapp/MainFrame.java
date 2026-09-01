package uk.co.raphel.railsim.configapp;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.TrackDiagramEntry;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.railsim.common.entity.Destination;
import uk.co.raphel.railsim.common.entity.RouteStop;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.StopType;
import uk.co.raphel.railsim.configapp.repository.BerthRepository;
import uk.co.raphel.railsim.configapp.repository.DestinationRepository;
import uk.co.raphel.railsim.configapp.repository.RouteStopRepository;
import uk.co.raphel.railsim.configapp.repository.TrainServiceRepository;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j(topic = "MainFrame")
@Component
public class MainFrame extends JFrame implements ActionListener, TableModelListener,
        ResourceLoaderAware {

    private final ServiceTableModel theTableModel = new ServiceTableModel();

    @Setter
    private ResourceLoader resourceLoader;


    private final DestinationRepository destinationRepository;
    private final BerthRepository berthRepository;
    private final RouteStopRepository routeStopRepository;
    private final TrainServiceRepository trainServiceRepository;

    private final List<TrackDiagramEntry> trackDiagram = new LinkedList<>();

    private List<Destination> destinations;
    private List<Berth> berths;
    private List<TrainService> trainServices;
    private final Map<Integer, Berth> berthLookup = new HashMap<>();

    private final List<EditableTrainService> editableTrainServices = new ArrayList<>();

    //   private boolean dataLoaded = false;

    private int currentDataPointer;
    private final JTextField txtService = new JTextField("");
    private final JTextField txtStart = new JTextField("");
    private final JTextField txtClass = new JTextField("");
    private final JTextField txtEquipment = new JTextField("");
    private final JTextField txtDest = new JTextField("");
    private final JPanel buttonPanel = new JPanel();
    private JComboBox<EditableTrainService> copyCombo;

    public MainFrame(DestinationRepository destinationRepository, BerthRepository berthRepository, RouteStopRepository routeStopRepository, TrainServiceRepository trainServiceRepository) {
        this.destinationRepository = destinationRepository;
        this.berthRepository = berthRepository;
        this.routeStopRepository = routeStopRepository;
        this.trainServiceRepository = trainServiceRepository;
    }


    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1200, 800));

        getDataFromDatabase();

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
        buttonPanel.setSize(600, 800);
        JButton nextButton = new JButton(">>");
        nextButton.setActionCommand("NEXT");
        nextButton.addActionListener(this);
        //nextButton.setEnabled(false);
        JButton prevButton = new JButton("<<");
        prevButton.setActionCommand("PREV");
        prevButton.addActionListener(this);
        //prevButton.setEnabled(false);
        JButton saveButton = new JButton("List All to File");
        saveButton.setActionCommand("LIST");
        saveButton.addActionListener(this);
        //saveButton.setEnabled(false);
        JButton exitButton = new JButton("Exit");
        exitButton.setActionCommand("EXIT");
        exitButton.addActionListener(this);
        JButton searchButton = new JButton("LOAD CSV");
        searchButton.setActionCommand("LOAD");
        searchButton.addActionListener(this);
        //searchButton.setEnabled(false);
        buttonPanel.setBorder(new TitledBorder("Actions"));
        buttonPanel.setLayout(new GridBagLayout());
        gridbag.setConstraints(buttonPanel, c);
        c.gridx = 0;
        c.gridy = 0;
        buttonPanel.add(nextButton, c);
        c.gridx = 1;
        c.gridy = 0;
        buttonPanel.add(prevButton, c);
        c.gridx = 2;
        c.gridy = 0;
        buttonPanel.add(searchButton, c);
        c.gridx = 3;
        c.gridy = 0;
        buttonPanel.add(saveButton, c);
        c.gridx = 4;
        c.gridy = 0;
        buttonPanel.add(exitButton, c);
        JLabel lblService = new JLabel("Service");
        JLabel lblStart = new JLabel("Start");
        JLabel lblClass = new JLabel("Class");
        JLabel lblEquipment = new JLabel("Engine");
        JLabel lblDest = new JLabel("Destination");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        buttonPanel.add(lblService, c);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        buttonPanel.add(txtService, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        buttonPanel.add(lblStart, c);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 3;
        buttonPanel.add(txtStart, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        buttonPanel.add(lblClass, c);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        buttonPanel.add(txtClass, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        buttonPanel.add(lblEquipment, c);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        buttonPanel.add(txtEquipment, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        buttonPanel.add(lblDest, c);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 3;
        buttonPanel.add(txtDest, c);

        copyCombo = new JComboBox<>();
        // TODO -- Fill combo from trainservices
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        buttonPanel.add(copyCombo, c);
        JButton copyButton = new JButton("Copy");
        copyButton.setActionCommand("COPY");
//        if (!dataLoaded) {
//            copyButton.setEnabled(false);
//        }
        copyButton.addActionListener(this);
        c.gridx = 3;
        c.gridy = 6;
        c.gridwidth = 1;
        buttonPanel.add(copyButton, c);

        JButton loadButton = new JButton("Load");
        loadButton.setActionCommand("LOAD");
        loadButton.addActionListener(this);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        buttonPanel.add(loadButton, c);

        add(buttonPanel);

        pack();

        setVisible(true);
        setState(Frame.NORMAL);
    }

    private void getDataFromDatabase() {
        berths = berthRepository.findAll(Sort.by(Sort.Direction.ASC, "berthId"));
        trainServices = trainServiceRepository.findAll(Sort.by(Sort.Direction.ASC, "startTime"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("NEXT")) {
            if (currentDataPointer < editableTrainServices.size() - 1) {
                currentDataPointer++;
                setCurrentDataToTableModel();
            }
        }
        if (e.getActionCommand().equals("PREV")) {
            if (currentDataPointer > 0) {
                currentDataPointer--;
                setCurrentDataToTableModel();
            }
        }
        if (e.getActionCommand().equals("LIST")) {
            try {
                File outFile = new File("ServiceList.txt");

                BufferedWriter br = new BufferedWriter(new FileWriter(outFile));

                for (TrainService trainService : trainServices) {
                    // Service 00:00 - START to END : Service Class: xxxxx  :  Engine: xxxxxxx
                    // Route point xx : Berth xxxxxx (nn) ARR: 00:00 (DEP 00:00 STOP/PASS/TERMINATE
                    br.write("Service " + trainService.getStartTime() + " - " + trainService.getOrigin() + " to " +
                            trainService.getDestination() + " : " + " Class: " + trainService.getServiceClass() +
                            "     Engine : " + trainService.getEngine());
                    br.newLine();
                    for (RouteStop stop : trainService.getRoutePoints()) {
                        br.write("RoutePoint " + stop.getRouteStopNumber() + " : Berth " + stop.getBerth().getBerthName() +
                                "(" + stop.getBerth().getBerthId() + ")  ARR: " + stop.getArrivalTime() +
                                " ( DEP " + stop.getDepartureTime() + ")   Type= " +
                                stop.getStopType().toString());
                        br.newLine();
                    }
                }
                br.close();
            } catch (Exception ex) {
                log.error("error writing output...",ex);
            }
        }

        if (e.getActionCommand().equals("EXIT")) {
            this.dispose();
            System.exit(0);
        }
        if (e.getActionCommand().equals("COPY")) {
            EditableTrainService srvToCopy = (EditableTrainService) copyCombo.getSelectedItem();
            if (srvToCopy != null) {
                EditableTrainService srvCopyInto = editableTrainServices.get(currentDataPointer);
                srvCopyInto.setCallingPoints(srvToCopy.getCallingPoints());
                //     setCurrentDataToTableModel();
            }
        }

        if (e.getActionCommand().equals("LOAD")) {
            log.info("Loading destinations with dummy berths");

            this.destinations = destinationRepository.findAll();
            this.berths = berthRepository.findAll();
            // trainServiceRepository.deleteAll();
            List<TrainService> upServices = loadServiceAndStops(getResource("classpath:formattedUpPlan.csv"));
            List<TrainService> downServices = loadServiceAndStops(getResource("classpath:formattedDownPlan.csv"));

            trainServiceRepository.saveAll(upServices);
            trainServiceRepository.saveAll(downServices);

            log.info("Loading complete.");
            // setup table data
            trainServiceRepository.findAll().forEach(trainService -> {
                editableTrainServices.add(trainToEditable(trainService));
            });
            setCurrentDataToTableModel();
            // dataLoaded = true;
        }
    }

    private EditableTrainService trainToEditable(TrainService train) {
        EditableTrainService ed= new EditableTrainService();
        ed.setStartBerth(train.getRoutePoints().get(0).getBerth());
        ed.setEngine(train.getEngine());
        ed.setCallingPoints(train.getRoutePointsAsMap());
        ed.setDestinationName(train.getDestination());
        ed.setStartName(train.getOrigin());
        ed.setStartTime(DateTimeFormatter.ofPattern("HH:mm").format(train.getStartTime()));
        ed.setDestBerth(train.getTerminalBerth());
        ed.setServiceClass(train.getServiceClass());
        return ed;
    }


    private LocalTime[] computeBerthTimes(String value) {
        LocalTime[] retval = {LocalTime.MIN, LocalTime.MIN};
        if (value.contains("/")) {
            if (value.startsWith("S") || value.startsWith("P")) {
                String[] parts = value.substring(1).split("/");
                try {
                    retval[0] = LocalTime.parse(parts[0].replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                    retval[1] = LocalTime.parse(parts[1].replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e) {
                    log.error("Error computing berth time : ",e);
                }
            } else {
                String[] parts = value.split("/");
                retval[0] = LocalTime.parse(parts[0].replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                retval[1] = LocalTime.parse(parts[1].replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } else {
            if (value.equalsIgnoreCase("P")) {
                return retval;
            } else {
                if (value.startsWith("S")) {
                    retval[0] = LocalTime.parse(value.replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                    retval[1] = retval[0].plusMinutes(1);
                } else {
                    try {
                        retval[0] = LocalTime.parse(value.replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                        retval[1] = retval[0].plusMinutes(0);
                    } catch (Exception e) {
                        log.error("In val = " + value);
                    }
                }
            }
        }
        return retval;
    }


    private StopType computeStopType(String value) {
        try {
            if (value.isEmpty()) {
                return StopType.PASS;
            }
            return StopType.getFromTimeTable(value.substring(0, 1));
        } catch (IllegalArgumentException e) {
            log.error("Invalid stop type " + value);
            return StopType.INVALID;
        }
    }

    private Berth findBerth(int start) {
        return berths.stream().filter(b -> b.getBerthId() == start).findFirst().orElse(null);
    }

    private void setCurrentDataToTableModel() {
        EditableTrainService srv = editableTrainServices.get(currentDataPointer);
        theTableModel.setData(srv);
        txtService.setText(srv.getStartTime());
        txtStart.setText(srv.getStartName());
        txtClass.setText(srv.getServiceClass());
        txtEquipment.setText(srv.getEngine());
        txtDest.setText(srv.getDestinationName());
        filterCopyCombo(srv);
        buttonPanel.invalidate();
        theTableModel.fireTableDataChanged();

    }

    private void filterCopyCombo(EditableTrainService srv) {

        if (copyCombo != null) {
            copyCombo.removeAllItems();
            editableTrainServices.stream().filter(serv -> serv.getStartName().equals(srv.getStartName())
                            && serv.getDestinationName().equals(srv.getDestinationName()))
                    .forEach(copyCombo::addItem);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column >= 0) {
            TableModel model = (TableModel) e.getSource();
            Object data = model.getValueAt(row, column);
            EditableTrainService toChange = editableTrainServices.get(currentDataPointer);
            toChange.getCallingPoints().put(trackDiagram.get(row).getId(), (String) data);
        }
    }


    private List<TrainService> loadServiceAndStops(Resource resource) {
        // Each Line has one of these formats :
        // Shh.mm:berth - Stop and then leave immediatly
        // Shh.mm/hh.mm - Stop and then leave at second time
        // P: -- Direct PassThru, skip
        // Phh.mm:berth  -- Pasthru at expected time, use normally
        // Thh.mm:berth  -- Terminate here, don't read the rest of the line
        //
        // In each line :
        // Ignore all entries until 1st entry with a hh.mm field
        // Ignore rest of line after 'T' event
        // Ignore all after 'P:'
        // if after line is translated, no 'T' entry exists, make last timed event type 'T'
        //
        List<TrainService> toReturn = new ArrayList<>();
        log.info("Loading services from " + resource.getFilename());

        try {
            File inFile = resource.getFile();

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            br.readLine();
            // we can lookup the reuired service and berth
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",", -1);
                if (splitLine.length >= 5) {
                    // creater train servicce from Train,From,Class,Engine,Destination
                    TrainService trainService = new TrainService();
                    trainService.setStartTime(LocalTime.parse(splitLine[0].replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm")));
                    trainService.setOrigin(splitLine[1]);
                    trainService.setDestination(splitLine[4]);
                    trainService.setServiceClass(splitLine[2]);
                    trainService.setEngine(splitLine[3]);
                    trainService.setRoutePoints(new ArrayList<>());
                    log.info("Processing train service " + trainService.getStartTime() + " -> " + trainService.getOrigin() + " -> " + trainService.getDestination());
                    // Fill in  the routepoints
                    boolean haveStart = false;
                    boolean haveTerminate = false;
                    int index = 5; // Poin to 1st train time slot
                    while (index < splitLine.length && !haveTerminate) {
                        // for (int s = 5; s < splitLine.length; s++) {
                        if (!splitLine[index].isEmpty()) {
                            String entry = splitLine[index];
                            if (!haveStart) {
                                if (entry.contains(".")) {
                                    haveStart = true;
                                }
                            }
                            if (haveStart) {
                                StopType stopType = StopType.getFromTimeTable(entry);
                                switch (stopType) {
                                    case PASS:
                                        if (entry.contains(".")) {
                                            RouteStop stop = buildRouteStop(trainService, entry);
                                            if (stop != null) {
                                                trainService.getRoutePoints().add(stop);
                                            }
                                        }
                                        break;
                                    case STOP:
                                        trainService.getRoutePoints().add(buildRouteStop(trainService, entry));
                                        break;
                                    case TERMINATE:
                                        trainService.getRoutePoints().add(buildRouteStop(trainService, entry));
                                        haveTerminate = true;
                                        break;
                                    case INVALID:
                                        break;
                                }
                            }
                        }
                        index++;
                    }
                    toReturn.add(trainService);
                }
            }
            br.close();
            for(TrainService trainService : toReturn) {
                trainService.getRoutePoints().sort(Comparator.comparing(RouteStop::getDepartureTime));
            }
            return toReturn;
        } catch (IOException e) {
            log.error("Failed to load services ", e);
            return new ArrayList<>();
        }
    }

    private RouteStop buildRouteStop(TrainService trainService, String entry) {
        if (entry.contains(".")) {
            if (entry.contains(":")) {
                RouteStop routeStop = new RouteStop();
                routeStop.setService(trainService);
                routeStop.setStopType(computeStopType(entry));
                routeStop.setBerth(findBerth(Integer.parseInt(entry.substring(entry.indexOf(":") + 1))));
                LocalTime[] times = computeBerthTimes(entry.substring(1, entry.indexOf(":")));
                routeStop.setDepartureTime(times[1]);
                routeStop.setArrivalTime(times[0]);
                routeStop.setRouteStopNumber(trainService.getRoutePoints().size()+1);
               // trainService.getRoutePoints().add(routeStop);
                return routeStop;
            }
        }
        return null;
    }

    private Resource getResource(String location) {
        return resourceLoader.getResource(location);
    }

}
