package tutorial.model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.amalgamasimulation.engine.Engine;
import com.amalgamasimulation.geometry.Point;
import com.amalgamasimulation.geometry.Polyline;
import com.amalgamasimulation.graphagent.GraphEnvironment;
import com.amalgamasimulation.utils.random.DefaultRandomGenerator;

import tutorial.Mapping;
import tutorial.scenario.Scenario;

public class Model extends com.amalgamasimulation.engine.Model {
	private final Scenario scenario;
	private final RandomGenerator randomGenerator = new DefaultRandomGenerator(1);
	private Mapping mapping = new Mapping();
	
	private List<Warehouse> warehouses = new ArrayList<>();
	private List<Store> stores = new ArrayList<>();
	private List<Truck> trucks = new ArrayList<>();
	private List<TransportationRequest> requests = new ArrayList<>();
	private final Dispatcher dispatcher;
	private final Statistics statistics;

	private GraphEnvironment<Node, Arc, Truck> graphEnvironment = new GraphEnvironment<>();
	private List<Arc> arcs = new ArrayList<>();
	
	public Model(Engine engine, Scenario scenario) {
		super(engine);
		engine.setTemporal(scenario.getBeginDate(), ChronoUnit.HOURS);
		engine.scheduleStop(engine.dateToTime(scenario.getEndDate()), "Stop");
		this.scenario = scenario;

		initializeModelObjects();
		
		RealDistribution newRequestIntervalDistribution = new ExponentialDistribution(randomGenerator, scenario.getIntervalBetweenRequestsHrs());
		RequestGenerator requestGenerator = new RequestGenerator(this, newRequestIntervalDistribution, scenario.getMaxDeliveryTimeHrs());
		requestGenerator.addNewRequestHandler(this::addRequest);

		dispatcher = new Dispatcher(this);
		requestGenerator.addNewRequestHandler(dispatcher::onNewRequest);
		statistics = new Statistics(this);
	}
	
	private void initializeModelObjects() {
		initializeGraph();
		initializeAssets();
		initializeTrucks();
	}

	private void initializeTrucks() {
		for (var scenarioTruck : scenario.getTrucks()) {
			Truck truck = new Truck(scenarioTruck.getId(), scenarioTruck.getName(), scenarioTruck.getSpeed(), engine());
			truck.setGraphEnvironment(graphEnvironment);
			Node homeNode = mapping.nodesMap.get(scenarioTruck.getInitialNode());
			truck.jumpTo(homeNode);
			trucks.add(truck);
		}
	}

	private void initializeGraph() {
		for (int i = 0; i < scenario.getNodes().size(); i++) {
			var scenarioNode = scenario.getNodes().get(i);
	        Node node = new Node(new Point(scenarioNode.x(), scenarioNode.y()));
	        graphEnvironment.addNode(node);
	        mapping.nodesMap.put(scenarioNode, node);
		}
		for (var scenarioArc : scenario.getArcs()) {
			Polyline polyline = createPolyline(scenarioArc);
			if (polyline.getLength() != 0) {
				Node sourceNode = mapping.nodesMap.get(scenarioArc.getSource());
				Node destNode = mapping.nodesMap.get(scenarioArc.getDest());

				Arc forwardArc = new Arc(polyline);
				Arc backwardArc = new Arc(polyline.getReversed());

				forwardArc.setReverseArc(backwardArc);
				backwardArc.setReverseArc(forwardArc);
				graphEnvironment.addArc(sourceNode, destNode, forwardArc, backwardArc);
				mapping.forwardArcsMap.put(scenarioArc, forwardArc);
				this.arcs.add(forwardArc);
				this.arcs.add(backwardArc);
			}
		}
	}
	
	private Polyline createPolyline(tutorial.scenario.Arc scenarioArc) {
		List<Point> points = new ArrayList<>();
		points.add(new Point(scenarioArc.getSource().x(), scenarioArc.getSource().y()));
		for (var bendpoint : scenarioArc.getPoints()) {
			points.add(new Point(bendpoint.x(), bendpoint.y()));
		}
		points.add(new Point(scenarioArc.getDest().x(), scenarioArc.getDest().y()));
		return new Polyline(points.stream().distinct().toList());
	}

	private void initializeAssets() {
		for (var scenarioWarehouse : scenario.getWarehouses()) {
			var wh = new Warehouse(mapping.nodesMap.get(scenarioWarehouse.getNode()), scenarioWarehouse.getName());
			warehouses.add(wh);
		}
		for (var scenarioStore : scenario.getStores()) {
			var store = new Store(mapping.nodesMap.get(scenarioStore.getNode()), scenarioStore.getName());
			stores.add(store);
		}
	}
	
	public RandomGenerator getRandomGenerator() {
		return randomGenerator;
	}
	
	public List<Truck> getTrucks() {
		return trucks;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	public List<Warehouse> getWarehouses() {
		return warehouses;
	}
	
	public List<Store> getStores() {
		return stores;
	}
	
	public List<TransportationRequest> getRequests() {
		return requests;
	}
	
	public void addRequest(TransportationRequest request) {
		requests.add(request);
	}
	
}
