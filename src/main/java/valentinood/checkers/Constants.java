package valentinood.checkers;

import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;

import java.io.File;

public final class Constants {
    public static final Background BACKGROUND_TILE_LIGHT = Background.fill(Paint.valueOf("#dbdbdb"));
    public static final Background BACKGROUND_TILE_DEFAULT = Background.fill(Paint.valueOf("#292929"));
    public static final Background BACKGROUND_TILE_HOVER = Background.fill(Paint.valueOf("#393939"));
    public static final Background BACKGROUND_TILE_HIGHLIGHTED = Background.fill(Paint.valueOf("#898989"));

    public static final FileChooser.ExtensionFilter FILTER_GAME_SNAPSHOT = new FileChooser.ExtensionFilter("Checkers game save", "*");

    public static final File CLASSDATA_PATH = new File("target/classes/");

    public static final int KEEP_ALIVE_INTERVAL_MILLIS = 10_000;

    private Constants() {}
}
