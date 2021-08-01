
import java.awt.Color;

public class Giant extends Critter {
    
    private int counter;
    
    public Giant(){
        counter =1;
    }
    
    public Color getColor(){
        return Color.GRAY;
    }

    public String toString(){
        if(counter>18){
            counter=1;
        }
        if (counter<=6){
            return "fee";
        }
        else if(counter<=12){
            return "fie";  
        }
        return "fum";
        
    }
 
    public Action getMove(CritterInfo info){
       
        counter++;
        if (info.getFront()==Neighbor.OTHER){
            return Action.INFECT;
        }
       else if(info.getFront()==Neighbor.EMPTY){
            return Action.HOP;
        }
        else{
            return Action.RIGHT;
        }
    }
}