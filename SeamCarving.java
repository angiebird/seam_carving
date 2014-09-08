import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static java.lang.Math.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


class Pixel {
    static int up = 0;
    static int left = 1;
    static int down = 2;
    static int right = 3;
    Pixel[] dir = new Pixel[4]; //0:up 1:left 2:down 3:right
    Color c;
    int e;
    int dp;
    int chg;
    int mark;
    Pixel bt; // back tracking
    Pixel ft; // forward tracking


    public Pixel(){
        c = new Color(0);
    }
    public Pixel(int rgb){
        c = new Color(rgb);
    }
    static public void setDir(int rc){
        up = 1-rc;
        left = rc;
        down = 2+1-rc;
        right = 2+rc;
    }
    static public Pixel img2graph(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();
        Pixel p = new Pixel(img.getRGB(0, 0));
        Pixel root = p;
        for(int h = 0; h < height; h++){
            if(h > 0){
                // go down
                if(h%2 == 0){
                    p.dir[down] = new Pixel(img.getRGB(0, h));
                }
                else{
                    p.dir[down] = new Pixel(img.getRGB(width-1, h));
                }
                p.dir[down].dir[up] = p;
                p = p.dir[down];
            }
            if(h%2 == 0){
                // go right
                for(int w = 1; w < width; w++){
                    p.dir[right] = new Pixel(img.getRGB(w, h));
                    p.dir[right].dir[left] = p;
                    if(h > 0){
                        p.dir[right].dir[up] = p.dir[up].dir[right];
                        p.dir[up].dir[right].dir[down] = p.dir[right];
                    }
                    p = p.dir[right];
                }
            }
            else{
                // go left
                for(int w = width-2; w >= 0; w--){
                    p.dir[left] = new Pixel(img.getRGB(w, h));
                    p.dir[left].dir[right] = p;
                    if(h > 0){
                        p.dir[left].dir[up] = p.dir[up].dir[left];
                        p.dir[up].dir[left].dir[down] = p.dir[left];
                    }
                    p = p.dir[left];
                }
            }
        }
        return root;
    }
    static public BufferedImage graph2img(Pixel root){
        //get width
        Pixel p = root;
        int width = 1;
        while(p.dir[right] != null){
            width++;
            p = p.dir[right];
        }
        //get height
        p = root;
        int height = 1;
        while(p.dir[down] != null){
            height++;
            p = p.dir[down];
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Pixel ph = root;
        Pixel pw = root;
        for(int h = 0; h < height; h++){
            if(h > 0){
                ph = ph.dir[down];
            }
            pw = ph;
            for(int w = 0; w < width; w++){
                if(w > 0){
                    pw = pw.dir[right];
                }
                img.setRGB(w, h, pw.c.getRGB());
            }
        }
        return img;
    }
    static public int absGrad(Pixel a, Pixel b){
        return abs(a.c.getRed()   - b.c.getRed())   + 
               abs(a.c.getGreen() - b.c.getGreen()) + 
               abs(a.c.getBlue()  - b.c.getBlue());
    }
    public void energy(){
        this.e = 0;
        if(this.dir[left] != null){
            this.e += absGrad(this, this.dir[left]);
        }
        else if(this.dir[right] != null){
            this.e += absGrad(this, this.dir[right]);
        }

        if(this.dir[up] != null){
            this.e += absGrad(this, this.dir[up]);
        }
        else if(this.dir[down] != null){
            this.e += absGrad(this, this.dir[down]);
        }
    }
    static public void calEnergy(Pixel root){
        Pixel ph = root;
        Pixel pw = root;
        while(ph != null){
            pw = ph;
            while(pw != null){
                pw.bt = null;
                pw.ft = null;
                pw.energy();
                if(pw.dir[up] != null){
                    pw.dp = pw.dir[up].dp;
                    pw.bt = pw.dir[up];
                    if(pw.dir[up].dir[left] != null && pw.dir[up].dir[left].dp < pw.dp){
                        pw.dp = pw.dir[up].dir[left].dp;
                        pw.bt = pw.dir[up].dir[left];
                    }
                    if(pw.dir[up].dir[right] != null && pw.dir[up].dir[right].dp < pw.dp){
                        pw.dp = pw.dir[up].dir[right].dp;
                        pw.bt = pw.dir[up].dir[right];
                    }
                    pw.bt.ft = pw;
                }
                pw.dp += pw.e;
                pw = pw.dir[right];
            }
            ph = ph.dir[down];
        }
    }
    static public void chkEnergy(Pixel root){
        Pixel ph = root;
        Pixel pw = root;
        int error = 0;
        while(ph != null){
            pw = ph;
            while(pw != null){
                pw.energy();
                int tdp = 0;
                if(pw.dir[up] != null){
                    tdp = pw.dir[up].dp;
                    if(pw.dir[up].dir[left] != null && pw.dir[up].dir[left].dp < tdp){
                        tdp = pw.dir[up].dir[left].dp;
                    }
                    if(pw.dir[up].dir[right] != null && pw.dir[up].dir[right].dp < tdp){
                        tdp = pw.dir[up].dir[right].dp;
                    }
                }
                tdp += pw.e;
                if(tdp != pw.dp){
                    pw.c = new Color(0,0,255);
                    error++;
                    System.out.println("Error: " + error);
                }
                pw = pw.dir[right];
            }
            ph = ph.dir[down];
        }
    }
    static public void mkColumn(Pixel root){
        Pixel p = root;
        // find the bottom left pixel
        while(p.dir[down] != null){
            p = p.dir[down];
        }

        // find the minimum end
        Pixel min = p;
        int i = 0;
        while(p != null){
            if(p.dp < min.dp){
                min = p;
            }
            p = p.dir[right];
        }

        // back tracking
        Pixel curr = min;
        while(curr != null){
            curr.c = new Color(255, 0, 0);
            curr = curr.bt;
        }
    }
    static public Pixel rmColumn(Pixel root){
        Pixel p = root;
        // find the bottom left pixel
        while(p.dir[down] != null){
            p = p.dir[down];
        }

        // find the minimum end
        Pixel min = p;
        int i = 0;
        while(p != null){
            if(p.dp < min.dp){
                min = p;
            }
            p = p.dir[right];
        }

        // back tracking
        Pixel curr = min;
        Pixel first = min;
        while(curr != null){
            if(curr.bt != null){
                if(curr.dir[up].dir[left] == curr.bt){
                    curr.dir[up].dir[down] = curr.dir[left];
                    curr.dir[left].dir[up] = curr.dir[up];
                }
                else if(curr.dir[up].dir[right] == curr.bt){
                    curr.dir[up].dir[down] = curr.dir[right];
                    curr.dir[right].dir[up] = curr.dir[up];
                }
            }
            if(curr.dir[left] != null){
                curr.dir[left].dir[right] = curr.dir[right];
                curr.dir[left].chg = 1;
                //curr.dir[left].energy(); // update energy --> do this in updateEnergy()
            }
            if(curr.dir[right] != null){
                curr.dir[right].dir[left] = curr.dir[left];
                curr.dir[right].chg = 1;
                //curr.dir[right].energy(); // update energy --> do this in updateEnergy()
            }
            first = curr;
            curr = curr.bt;
        }
        if(first == root){
            System.out.println("root changes");
            root = root.dir[right];
        }
        return first;
    }
    static public void updateEnergy(Pixel first){
        // forward tracking
        // update dp
        Queue q = new LinkedList();
        Iterator iterator = q.iterator();
        if(first.dir[left] != null){
            first.dir[left].mark = 1;
            q.add(first.dir[left]);
        }
        if(first.dir[right] != null){
            first.dir[right].mark = 1;
            q.add(first.dir[right]);
        }

        Pixel cc;
        while(q.isEmpty() == false){
            cc = (Pixel)q.remove();
            cc.mark = 0;
            if(cc.chg == 1){
                cc.energy();
            }
            int tdp = 0;
            Pixel tbt = null;
            if(cc.dir[up] != null){
                tdp = cc.dir[up].dp;
                tbt = cc.dir[up];
                if(cc.dir[up].dir[left] != null && cc.dir[up].dir[left].dp < tdp){
                    tdp = cc.dir[up].dir[left].dp;
                    tbt = cc.dir[up].dir[left];
                }
                if(cc.dir[up].dir[right] != null && cc.dir[up].dir[right].dp < tdp){
                    tdp = cc.dir[up].dir[right].dp;
                    tbt = cc.dir[up].dir[right];
                }
            }
            tdp += cc.e;
            if(cc.chg == 1 || cc.dp != tdp || cc.bt != tbt){
                //cc.c = new Color(0,255,0); --> this will affect the energy and dp update
                cc.chg = 0;
                cc.dp = tdp;
                cc.bt = tbt;
                if(cc.bt != null){
                    cc.bt.ft = cc;
                }
                if(cc.dir[down] != null){
                    if(cc.dir[down].mark == 0){
                        cc.dir[down].mark = 1;
                        q.add(cc.dir[down]);
                    }
                    if(cc.dir[down].dir[left] != null && cc.dir[down].dir[left].mark == 0){
                        cc.dir[down].dir[left].mark = 1;
                        q.add(cc.dir[down].dir[left]);
                    }
                    if(cc.dir[down].dir[right] != null && cc.dir[down].dir[right].mark == 0){
                        cc.dir[down].dir[right].mark = 1;
                        q.add(cc.dir[down].dir[right]);
                    }
                }
            }
        }
    }
}

class SeamCarving extends JPanel{
    static JFrame frame = new JFrame();
    static BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    static int setImg = 0;
    public void paint(Graphics g) {
        BufferedImage img = createImage();
        g.drawImage(img, 0,0,this);
    }
    private BufferedImage createImage(){
        if(setImg == 0){
            try{
                File input = new File("pic.png");
                img = ImageIO.read(input);
                setImg = 1;
            }
            catch (Exception e){}
        }

        Pixel.setDir(1);
        Pixel root = Pixel.img2graph(img);
        Dimension size = frame.getSize();
        Pixel.calEnergy(root);
        int width = (int)size.getWidth();
        for(int i = 0; i < img.getWidth() - width; i++){
            Pixel first = Pixel.rmColumn(root);
            Pixel.updateEnergy(first);
            //Pixel.chkEnergy(root);
        }

        Pixel.setDir(0);
        Pixel.calEnergy(root);
        int height = (int)size.getHeight();
        for(int i = 0; i < img.getHeight() - height; i++){
            Pixel first = Pixel.rmColumn(root);
            Pixel.updateEnergy(first);
            //Pixel.chkEnergy(root);
        }

        Pixel.setDir(1);

        BufferedImage img2 = Pixel.graph2img(root);
        return img2;
    }
    static public void main(String args[]){
        SeamCarving sc = new SeamCarving();
        frame.getContentPane().add(sc);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setUndecorated(true);

        MoveMouseListener mml = new MoveMouseListener(sc);
        sc.addMouseListener(mml);
        sc.addMouseMotionListener(mml);

        frame.setVisible(true);

    }
}

class MoveMouseListener implements MouseListener, MouseMotionListener {
    JComponent target;
    Point start_drag;
    Point start_loc;
    Dimension size;

    public MoveMouseListener(JComponent target) {
        this.target = target;
    }

    public static JFrame getFrame(Container target) {
        if (target instanceof JFrame) {
            return (JFrame) target;
        }
        return getFrame(target.getParent());
    }

    Point getScreenLocation(MouseEvent e) {
        Point cursor = e.getPoint();
        Point target_location = this.target.getLocationOnScreen();
        return new Point((int) (target_location.getX() + cursor.getX()),
                (int) (target_location.getY() + cursor.getY()));
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        this.start_drag = this.getScreenLocation(e);
        JFrame frame = this.getFrame(target);
        this.start_loc = frame.getLocation();
        this.size = frame.getSize();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        Point current = this.getScreenLocation(e);
        Point offset = new Point((int) current.getX() - (int) start_drag.getX(), (int) current.getY() - (int) start_drag.getY());
        JFrame frame = this.getFrame(target);
        Dimension windowSize = new Dimension((int)(size.getWidth()+offset.getX()), (int)(size.getHeight()+offset.getY()));
        frame.setSize(windowSize);
    }

    public void mouseMoved(MouseEvent e) {
    }
}
