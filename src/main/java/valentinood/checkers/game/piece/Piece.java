package valentinood.checkers.game.piece;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Piece {
    private PieceType type;
    private ImageView imageView;

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
                '}';
    }
}
