package valentinood.checkers.game.piece;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;

public class Piece implements Serializable {
    private PieceType type;
    private transient ImageView imageView;

    public Piece() {
    }

    public Piece(PieceType type) {
        this.type = type;
        loadImage();
    }

    private void loadImage() {
        imageView = new ImageView(new Image(type.getResource().toString()));
        imageView.setPreserveRatio(true);
    }

    public PieceType getType() {
        return type;
    }

    public PieceTeam getTeam() {
        return type.getTeam();
    }

    public ImageView getImageView() {
        if (imageView == null) loadImage();

        return imageView;
    }

    public boolean isKing() {
        return type.isKing();
    }

    public void setKing(boolean king) {
        if (king) {
            if (getTeam() == PieceTeam.Red) {
                type = PieceType.RedKing;
            } else {
                type = PieceType.BlueKing;
            }
        } else {
            if (getTeam() == PieceTeam.Red) {
                type = PieceType.Red;
            } else {
                type = PieceType.Blue;
            }
        }
        loadImage();
    }

    @Override
    public String toString() {
        return "Piece{" +
                "type=" + type +
                ", imageView=" + imageView +
                '}';
    }
}
