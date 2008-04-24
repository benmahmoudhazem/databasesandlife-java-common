
package adrian.util.gui.swing;

import java.awt.*;
import javax.swing.*;

public class JCenteredUnderlinedLabel extends JLabel  {
 public JCenteredUnderlinedLabel(){
  this("");    
  }

 public JCenteredUnderlinedLabel(String text){
  super(text);    
  setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
  }

 public void paint(Graphics g) {
  Rectangle r;
  super.paint(g);
  r = g.getClipBounds();
  
  int width = this.getFontMetrics(this.getFont()).stringWidth(this.getText());
  int offset = (getWidth() - width)/2;
  
  g.drawLine
   (offset,
    r.height - this.getFontMetrics(this.getFont()).getDescent() +1, 
    offset + width,  
    r.height - this.getFontMetrics(this.getFont()).getDescent() +1);
  }
}