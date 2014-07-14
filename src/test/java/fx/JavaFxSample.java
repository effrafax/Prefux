package fx;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import prefux.FxDisplay;
import prefux.Visualization;
import prefux.action.ActionList;
import prefux.action.RepaintAction;
import prefux.action.layout.graph.ForceDirectedLayout;
import prefux.activity.Activity;
import prefux.controls.DragControl;
import prefux.data.Graph;
import prefux.data.io.DataIOException;
import prefux.data.io.GraphMLReader;
import prefux.render.CombinedRenderer;
import prefux.render.DefaultRendererFactory;
import prefux.render.LabelRenderer;
import prefux.render.ShapeRenderer;

public class JavaFxSample extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	private static final double WIDTH = 300;
	private static final double HEIGHT = 250;

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("Hello World!");
		BorderPane root = new BorderPane();
		primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
		primaryStage.show();
		
		Graph graph = null;
		try {
//		    graph = new GraphMLReader().readGraph("data/graphml-sample.xml");
    	    graph = new GraphMLReader().readGraph("data/socialnet.xml");
		    Visualization vis = new Visualization();
		    vis.add("graph", graph);
		    
		    ShapeRenderer sr = new ShapeRenderer();
		    LabelRenderer lr = new LabelRenderer("name");
		    lr.translate(5.0, 5.0);
		    // LabelRenderer lr2 = new LabelRenderer("name");
		    // lr2.addStyle("invisible");
		    // BorderPaneRenderer r = new BorderPaneRenderer();
		    CombinedRenderer r = new CombinedRenderer();
		    r.add(lr);
		    r.add(sr);

		    // create a new default renderer factory
		    // return our name label renderer as the default for all non-EdgeItems
		    // includes straight line edges for EdgeItems by default
		    vis.setRendererFactory(new DefaultRendererFactory(sr));
		    
		    ActionList layout = new ActionList(Activity.INFINITY);
		    layout.add(new ForceDirectedLayout("graph"));
		    layout.add(new RepaintAction());
		    vis.putAction("layout", layout);
		    FxDisplay display = new FxDisplay(vis);
		    display.addControlListener(new DragControl());
		    root.setCenter(display);
		    root.setBottom(buildControlPanel(display));
		    vis.run("layout");

		} catch ( DataIOException e ) {
		    e.printStackTrace();
		    System.err.println("Error loading graph. Exiting...");
		    System.exit(1);
		}
	}
	
	private Node buildControlPanel(FxDisplay display) {
		VBox vbox = new VBox();
		Label txt = new Label("Zoom Factor");
		Slider slider = new Slider(0.0,10.0,1.0);
		Label txt2 = new Label("");
		display.zoomFactorProperty().bind(slider.valueProperty());
		vbox.getChildren().addAll(txt, slider, txt2);
		return vbox;
		
		
	}

}