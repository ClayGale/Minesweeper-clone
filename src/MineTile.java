
//Clays minesweeper project
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;


public class MineTile extends Button implements EventHandler<ActionEvent>{

	/**
	 * 
	 */
	int x;
	int y;
	int neighborMineCount = 0;
	boolean minePresent; //states whether the tile contains a mine
	boolean exposed = false;
	boolean flagged = false;
	boolean originTile = false; //used to determine which tile was the first clicked by the user.

	ImageView shownSprite = new ImageView();
	ImageView fadeSprite = new ImageView(MineSweeper1.blankTile);
	ImageView targetSprite = new ImageView();
	
	public MineTile(int x, int y, boolean m) {
		this.x = x;
		this.y = y;
		this.minePresent = m;

		double size = 32;
		this.setMinWidth(size);
		this.setMaxWidth(size);
		this.setMinHeight(size);
		this.setMaxHeight(size);

		this.shownSprite.imageProperty().setValue(MineSweeper1.blankTile);
		this.setGraphic(shownSprite);

	}
	
	public void updateImage() {
		if(this.minePresent) {
			this.explode();
			this.shownSprite.setImage(MineSweeper1.mineType[2]);
		}
		else if(!this.minePresent) {
			this.shownSprite.setImage(MineSweeper1.numbers[neighborMineCount]);
		}
	}

	@Override
	public void handle(ActionEvent event) {
		// Click handling done in MineSweeper1.java class
	}

	@Override
	public String toString(){
		return (x + " " + y);
	}

	public boolean isExposed() {
		return exposed;
	}

	public void setExposed(boolean exposed) {
		this.exposed = exposed;
		this.setFlagged(false);
		if(this.isMinePresent()) //Ensures that the mine always causes a loss if exposed
			this.explode();
	}

	public boolean isMinePresent() {
		return minePresent;
	}

	public void setMinePresent(boolean minePresent) {
		this.minePresent = minePresent;
	}

	public boolean isOriginTile() {
		return originTile;
	}

	public void circleOriginTile(boolean originTile) {
		if(this.originTile == false ) {
			this.originTile = originTile;
		}

		for(int i = this.x-1; i <= this.x+1; i++) {
			for(int j = this.y-1; j <= this.y+1; j++) { //below condition is index out of bounds checking
				if(i >= 0 && i < MineSweeper1.fieldWidth && j >= 0 && j < MineSweeper1.fieldHeight) {
					MineSweeper1.field[i][j].setOriginTile(true);
				}
			}
		}
	}
	private void setOriginTile(boolean originTile) {
		this.originTile = originTile;
	}

	public void spreadBlankSpace() { //this method is used every time the player clicks on a covered tile
		boolean spreadable = (this.getNeighborMineCount() == 0); 

		if(!this.isFlagged()) { //prevents tiles that are flagged from being open
			this.setExposed(true);
			this.updateImage(); //uncovers the tile in case this one was directly clicked
		}

		if(spreadable) { 
			for(int i = this.x-1; i <= this.x+1; i++) {			 
				for(int j = this.y-1; j <= this.y+1; j++) { //below condition is index out of bounds checking + making sure the tile is covered.
					if(i >= 0 && i < MineSweeper1.fieldWidth && j >= 0 && j < MineSweeper1.fieldHeight && !MineSweeper1.field[i][j].isExposed()) {
						if(spreadable && !MineSweeper1.field[i][j].isFlagged()){//If this tile was blank then it uncovers all adjacent tiles
							MineSweeper1.field[i][j].setExposed(true);
							MineSweeper1.field[i][j].updateImage();
							if(MineSweeper1.field[i][j].getNeighborMineCount() == 0 && !MineSweeper1.field[i][j].isFlagged())//recurses to other tiles is they are also blank
								MineSweeper1.field[i][j].spreadBlankSpace();
						}
					}
				}
			}
		}
	}

	public void uncoveredNumberClick() {
		int flagCount = 0;
		
		for(int i = this.x-1; i <= this.x+1; i++) {			 
			for(int j = this.y-1; j <= this.y+1; j++) {
				if(i >= 0 && i < MineSweeper1.fieldWidth && j >= 0 && j < MineSweeper1.fieldHeight) { 
					if(MineSweeper1.field[i][j].isFlagged()) 
						flagCount++;
				}
			}
		}
		//Second for loop only runs if there are the correct number of flags surrounding the clicked tile. If one of the flags is incorrect then the player still loses.
		if(flagCount == this.getNeighborMineCount()) {
			for(int i = this.x-1; i <= this.x+1; i++) {			 
				for(int j = this.y-1; j <= this.y+1; j++) {
					if(i >= 0 && i < MineSweeper1.fieldWidth && j >= 0 && j < MineSweeper1.fieldHeight)  {
						if(!MineSweeper1.field[i][j].isFlagged())
							MineSweeper1.field[i][j].spreadBlankSpace();
					}
				}
			}
		}
	}

	public int getNeighborMineCount() {
		return neighborMineCount;
	}

	public void setNeighborMineCount(int neighborMineCount) {
		this.neighborMineCount = neighborMineCount;
	}

	public boolean isFlagged() {
		return flagged;
	}

	public void setFlagged(boolean flagging) {
		if(!this.isExposed()) { //tile can only be flagged if it is not exposed
			this.flagged = flagging;
			if(this.isFlagged()) {
				this.shownSprite.setImage(MineSweeper1.flag);
			}
			else if (!this.isFlagged()){
				this.shownSprite.setImage(MineSweeper1.blankTile);
			}
		}
	}

	public void explode() {

		this.shownSprite.setImage(MineSweeper1.mineType[0]);
		if(MineSweeper1.gameOver == false)
			MineSweeper1.lose(); //eliminates any edge cases where mines could explode without causing a loss

	}

	public void misFlagged() { 
		this.shownSprite.setImage(MineSweeper1.mineType[1]);		
	}
}