package uk.co.raphel.railsim.configapp;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.TrackDiagramEntry;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.railsim.common.entity.Destination;
import uk.co.raphel.railsim.common.entity.RouteStop;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.StopType;
import uk.co.raphel.railsim.common.enums.TrackDirection;
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
import java.util.stream.IntStream;

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
    private List<Integer> berthLookup = new LinkedList<>();

    private final List<EditableTrainService> editableTrainServices = new ArrayList<>();

    private boolean dataLoaded = false;

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

        //getDataFromDatabase();

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
        nextButton.setEnabled(false);
        JButton prevButton = new JButton("<<");
        prevButton.setActionCommand("PREV");
        prevButton.addActionListener(this);
        prevButton.setEnabled(false);
        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(this);
        saveButton.setEnabled(false);
        JButton exitButton = new JButton("Exit");
        exitButton.setActionCommand("EXIT");
        exitButton.addActionListener(this);
        JButton searchButton = new JButton("Find MT");
        searchButton.setActionCommand("FIND");
        searchButton.addActionListener(this);
        searchButton.setEnabled(false);
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
        if (!dataLoaded) {
            copyButton.setEnabled(false);
        }
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

    @Override
    public void actionPerformed(ActionEvent e) {
     /*   if (e.getActionCommand().equals("NEXT")) {
            updateServiceHeader();
            if (currentDataPointer < editableTrainServices.size() - 1) {
                currentDataPointer++;
                setCurrentDataToTableModel();
            }
        }
        if (e.getActionCommand().equals("PREV")) {
            updateServiceHeader();
            if (currentDataPointer > 0) {
                currentDataPointer--;
                setCurrentDataToTableModel();
            }
        }
        if (e.getActionCommand().equals("SAVE")) {
            updateServiceHeader();
            saveOutput();
        }*/
        if (e.getActionCommand().equals("EXIT")) {
            this.dispose();
            System.exit(0);
        }
        if (e.getActionCommand().equals("FIND")) {
            currentDataPointer = findfirstEmpty();
            setCurrentDataToTableModel();
        }
        if (e.getActionCommand().equals("COPY")) {
            EditableTrainService srvToCopy = (EditableTrainService) copyCombo.getSelectedItem();
            if (srvToCopy != null) {
                EditableTrainService srvCopyInto = editableTrainServices.get(currentDataPointer);
                srvCopyInto.setCallingPoints(srvToCopy.getCallingPoints());
                setCurrentDataToTableModel();
            }
        }
        if (e.getActionCommand().equals("LOAD")) {
            log.info("Loading destinations with dummy berths");
            loadDestinations();
            loadBerths();
            loadServices(getResource("classpath:upPlan.csv"));
            loadServices(getResource("classpath:downPlan.csv"));
            conTrainServices();

            List<RouteStop>  stops = fillRouteStops(trainServices);
              log.info("Save routestops");
            routeStopRepository.saveAllAndFlush(stops);
            log.info("Saving berths to database");
            berthRepository.saveAllAndFlush(berths);
            log.info("Saving destinations to database");
            destinationRepository.saveAllAndFlush(destinations);
            log.info("Saving train services to database");
            trainServiceRepository.saveAllAndFlush(trainServices);

            log.info("Loading complete.");
            dataLoaded = true;
        }

    }

    private void conTrainServices() {
        // Copy editable services to entities
        log.info("Creating Train Service entities...");
        trainServices = editableTrainServices.stream()
                .map(this::ed2Entity)
                .collect(Collectors.toList());
    }
    private List<RouteStop> fillRouteStops(List<TrainService> trainServices) {
        log.info("Filling routestops from services");
        List<RouteStop> list = new ArrayList<>();
        for (TrainService srv : trainServices) {
           list.addAll(srv.getRoutePoints().stream().filter(s -> s.getBerth()!=null)
                    .toList());
        }
        return list;
    }

    private int findfirstEmpty() {
        if (!dataLoaded) {
            return 0;
        }
        for (int i = 0; i < editableTrainServices.size(); i++) {
            if (editableTrainServices.get(i).hasNoEntries()) {
                return i;
            }
        }
        return 0;
    }



    //
    // At this point destinations is filled, but with dummy berths (Just their indexes set)
    // Load all berths, If already know, then replace, otherwise crete new
    // Then, properly fill destination berths.
    private void loadBerths() {
        this.berths = new ArrayList<>();
        File inFile;
        List<TrackDiagramEntry> berthList = new ArrayList<>();
        try {
            log.info("Loading Trackmap (Berths)");
            try {
                inFile = getResource("classpath:TrackMapLive.csv").getFile();
                BufferedReader br = new BufferedReader(new FileReader(inFile));
                String line = br.readLine(); // SKIP HEADER
                while ((line = br.readLine()) != null) {
                    TrackDiagramEntry diagramEntry = new TrackDiagramEntry(line);
                    berthList.add(diagramEntry);

                }
                br.close();


            } catch (IOException e) {
                log.error("Error loading trackmap from " + "classpath:TrackMapLive.csv", e);
            }
            log.info("Trackmap  " + berthList.size() + " Berths");
        } catch (Exception e) {
            log.error("Error loading " + "classpath:TrackMapLive.csv", e);
        }
         // Generate trackmap base entities
        log.info("Generating Berth entities");
        for(TrackDiagramEntry diagramEntry : berthList) {
            berths.add(track2Berth(diagramEntry));
        }
        log.info("Setting up berth linking");
        for(TrackDiagramEntry diagramEntry : berthList) {
            Berth baseBerth = findBerth(diagramEntry.getId());
            if(baseBerth == null) {
                log.error("Base berth for track entry " +diagramEntry.getId() + " not found");
            } else {
                for (Integer berthLinkId : diagramEntry.getLinkedBerths()) {
                    Berth berth = findBerth(berthLinkId);
                    if (berth == null) {
                        log.error("Could not find berth " + berthLinkId +
                                " for diagram entry " + diagramEntry);
                    } else {
                        baseBerth.getPathTo().add(berth);
                    }
                }
            }
        }
    }

    private void loadDestinations() {
        File destinationsFile;
        try {
            destinationsFile = getResource("classpath:destinations.csv").getFile();
            // Skip first line
            destinations = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(destinationsFile));
            String line = br.readLine(); // SKIP HEADER
            while ((line = br.readLine()) != null) {
                destinations.add(new Destination(line));
            }
            br.close();
            log.info(destinations.size() + " destinations loaded");
        } catch (Exception e) {
            log.error("Error loading destinations", e);
        }
    }

    private void loadCSVFiles() {
        loadBerths();
        loadDestinations();
        // Reconcile berths and destinations
        log.info("Reconciling Berths to Destinations");
        for (Destination destination : destinations) {
            for (Berth b : destination.getHomeBerths()) {
                Berth realBerth = findBerth(Math.toIntExact(b.getBerthId()));
                if (realBerth != null) {
                    b.updateFrom(realBerth);
                } else {
                    log.error("Unable to find Berth " + b.getBerthId());
                }
            }
        }
        try {
            log.info("Loading up Schedule");
            loadServices(getResource("classpath:upPlan.csv"));
            log.info("Schedule Up " + this.editableTrainServices.size() + " Services");
        } catch (Exception e) {
            log.error("Error loading " + "classpath:upPlan.csv", e);
        }
        try {
            log.info("Loading down Schedule");
            loadServices(getResource("classpath:downPlan.csv"));
            log.info("Schedule Down, total =  " + editableTrainServices.size() + " Services");
        } catch (Exception e) {
            log.error("Error loading " + "classpath:downPlan.csv", e);
        }
        // Copy editable services to entities
        log.info("Creating Train Service entities...");
        trainServices = editableTrainServices.stream()
                .map(this::ed2Entity)
                .collect(Collectors.toList());

        // Fill out references
        log.info("DONE");

    }

    private Berth track2Berth(TrackDiagramEntry t) {
        Berth ret = new Berth();
        ret.setBerthId(t.getId().longValue());
        ret.setDirection(t.getTrackDirection());
        ret.setLengthMiles(t.getLength());
        ret.setMultiOccupancy(t.isAllowMultipleOccupancy());
        ret.setMilesFromOrigin(t.getDist());
        ret.setBerthName(t.getName());
        ret.setSpeedLimit(t.getSpeedLimit());
        return ret;
    }

    private TrainService ed2Entity(EditableTrainService ed) {
        TrainService ret = new TrainService();
        try {
            ret.setEngine(ed.getEngine());
            ret.setServiceClass(ed.getServiceClass());
            ret.setDestination(ed.getDestinationName());
            ret.setOrigin(ed.getStartName());
            ret.setStartTime(LocalTime.parse(ed.getStartTime().replaceAll("\\.", ":"), DateTimeFormatter.ofPattern("HH:mm")));
            ret.setRoutePoints(computeRoutePoints(ed.getCallingPoints()));
        } catch (Exception e) {
            log.error("Error converting ed --> Trainservice", e);
        }
        return ret;
    }

    private List<RouteStop> computeRoutePoints(Map<Integer, String> callingPoints) {
        List<RouteStop> ret = new ArrayList<>();
        // We have berth (int) and calling Data in key
        // P with target time = passing
        // P with no time, still passing
        // S with time = Stopping target
        // T with or without target termminate
        for (Map.Entry<Integer, String> entry : callingPoints.entrySet()) {
            if(!entry.getValue().equals("P")) {
                RouteStop t = new RouteStop();
                t.setBerth(findBerth(entry.getKey()));
                t.setService(TrainService.dummy());
                t.setStopType(computeStopType(entry.getValue()));
                t.setArrivalTime(computeArrivalTime(entry.getValue()));
                t.setDepartureTime(computeDepartureTime(entry.getValue()));
                ret.add(t);
            }
        }
        return ret;
    }

    private LocalTime computeDepartureTime(String value) {
        try {
            if (value.contains("/")) {
                String[] splitter = value.split("/");
                String deptime = splitter[1];
                return LocalTime.parse(deptime.replace(".", ":"), DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return computeArrivalTime(value);
            }
        } catch (Exception e) {
            log.error("Cannot compute departure time", e);
            return LocalTime.MIN;
        }
    }

    // S02.42/02.55
    private LocalTime computeArrivalTime(String value) {
        // Decimmal point ?
        if (value.contains(".")) {
            // We have something
            String initValue = value.substring(1);
            if (initValue.contains("/")) {
                // We have stop and depart
                initValue = initValue.substring(0, initValue.indexOf("/"));
                try {
                    return LocalTime.parse(initValue.replaceAll("\\.", ":"), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e) {
                    log.error("Error converting localtime " + value);
                    return LocalTime.MIN;
                }
            }
        }
        return LocalTime.MIN;
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


//        theTableModel.clear();
//        loadTrackMap(getResource("classpath:TrackMapUp.csv"));
//
//
//        theTableModel.setTrackNames(trackDiagram);
//        loadServices(getResource("classpath:ServicesUp1.csv"));
//        currentDataPointer = 0;
//        setCurrentDataToTableModel();
    //   }


    /*   private void saveOutput() {
           if (dataLoaded) {
               String outfileName = "c:\\temp.csv";

               try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outfileName))) {

                   // Write header
                   writer.write("Train,From,Class,Engine,Destination");

                   // Write list of tracksections
                   trackDiagram.forEach(t -> {
                       try {
                           writer.write("," + t.getId());
                       } catch (IOException ignore) {

                       }
                   });
                   writer.write("\r\n");

                   // Write each service
                   editableTrainServices.forEach(srv -> {
                       try {
                           writer.write(srv.getStartTime() + "," + srv.getStart() + "," + srv.getServiceClass() + "," +
                                   srv.getEngine() + "," + srv.getDestination());
                           trackDiagram.forEach(dia -> {
                               try {
                                   if (srv.getCallingPoints().containsKey(dia.getId())) {

                                       writer.write("," + srv.getCallingPoints().get(dia.getId()));
                                   } else {
                                       writer.write(",");
                                   }
                               } catch (IOException ignore) {

                               }
                           });
                           writer.write("\r\n");
                       } catch (IOException ignore) {

                       }
                   });
               } catch (Exception e) {
                   e.printStackTrace();
                   System.exit(1);
               }
           }
       }
   */
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

    private List<TrackDiagramEntry> loadTrackMap(Resource trackMap) {
        List<TrackDiagramEntry> toReturn = new ArrayList<>();
        try {
            File inFile = trackMap.getFile();
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            String line = br.readLine(); // SKIP HEADER
            while ((line = br.readLine()) != null) {
                TrackDiagramEntry diagramEntry = new TrackDiagramEntry(line);
                toReturn.add(diagramEntry);

            }
            br.close();


        } catch (IOException e) {
            log.error("Error loading trackmap from " + trackMap.getFilename(), e);
        }
        trackDiagram.addAll(toReturn);
        return toReturn;
    }

    private void loadServices(Resource resource) {
        // Train,From,Class,Engine,Destination,1,2,3,4,5,6,7,8,9,10,11,200,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,201,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81

        log.info("Loading services from " + resource.getFilename());

        try {
            File inFile = resource.getFile();

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            // Read the header line and index the track sections
            String headerLine = br.readLine();
            // set up lookupp list, with dummy entries at staff to align items correctly
            this.berthLookup = IntStream.generate(() -> 0)
                    .limit(5)
                    .boxed()
                    .collect(Collectors.toCollection(LinkedList::new));
            if (headerLine != null && !headerLine.isEmpty()) {
                String[] indexes = headerLine.split(",", -1);
                for (int i = 5; i < indexes.length; i++) {
                    int trackSection = Integer.parseInt(indexes[i]);
                    berthLookup.add(trackSection);
                }
            }
            // we can lookup the reuired berth id
            while ((line = br.readLine()) != null) {
                // Only read lines with service defined (may be being built still!)
                if (line.split(",", -1).length >= 5) {
                    editableTrainServices.add(getEditableTrainService(line));
                }
            }
            br.close();
        } catch (IOException e) {
            log.error("Failed to load services ", e);
        }
    }

    private EditableTrainService getEditableTrainService(String csvLine) {
        // e.g
        // Train,From,Class,Engine,Destination,1,2,3,4,5,6,7,8,9,10,11,200,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,201,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81
        //  00:25,Victoria,Pass,EMU,,S00.25,,,,S00.31,,S00.35,,,S00.38,S00.40,,S00.44,S00.46,,,T00.48,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
        String[] csv = csvLine.split(",", -1);

        EditableTrainService ret = new EditableTrainService();
        ret.setStartTime(csv[0]);
        ret.setStartBerth(findStartBerth(csv));
        ret.setStartName(csv[1]);
        ret.setDestBerth(findTBerth(csv));
        ret.setDestinationName(csv[4]);
        ret.setServiceClass(csv[2]);
        ret.setEngine(csv[3]);
        ret.setCallingPoints(findCallingPoints(berthLookup, csv));
        return ret;
    }

    private Berth findStartBerth(String[] csv) {
        int index = 5;
        while (index < csv.length && csv[index].isEmpty()) {
            index++;
        }
        if (index < csv.length) {
            return findBerth(berthLookup.get(index));
        }
        return new Berth();
    }

    private Berth findTBerth(String[] csv) {
        int index = 5;
        int latestIndexFilled = 0;
        while (index < csv.length && !csv[index].startsWith("T")) {
            if (!csv[index].isEmpty()) {
                latestIndexFilled = index;
            }
            index++;
        }
        if (index < csv.length) {
            // fround 'T' type
            try {
                return findBerth(berthLookup.get(index));
            } catch (IndexOutOfBoundsException e) {
                log.error("Failed to find Berth at index {} ", index);
            }
        }
        return findBerth(berthLookup.get(latestIndexFilled));

    }

    // Key will be stopping berthe id, String will be timefield
    private Map<Integer, String> findCallingPoints(List<Integer> indexList, String[] csv) {
        Map<Integer, String> ret = new HashMap<>();
        for (int i = 5; i < csv.length; i++) {
            if(!csv[i].isEmpty()) {
                ret.put(indexList.get(i), csv[i]);
            }
        }
        return ret;
    }


    private Resource getResource(String location) {
        return resourceLoader.getResource(location);
    }

}
