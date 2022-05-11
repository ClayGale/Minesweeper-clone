

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Face extends ImageView{

	Image[] faces = {new Image("/face-smile.png"),
			new Image("/face-dead.png"),
			new Image("/face-win.png"),
			new Image("/face-O.png")};
	
	public Face() {
		this.setImage(faces[0]);
	}
	public void reset() {
		this.setImage(faces[0]);
	}
	public void die() {
		this.setImage(faces[1]);
	}
	public void win() {
		this.setImage(faces[2]);
	}
	public void oFace() {
		this.setImage(faces[3]);
	}
}
