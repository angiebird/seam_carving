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
    Pixel up;
    Pixel down;
    Pixel left;
    Pixel right;
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
    static public Pixel img2graph(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();
        Pixel p = new Pixel(img.getRGB(0, 0));
        Pixel root = p;
        for(int h = 0; h < height; h++){
            if(h > 0){
                // go down
                if(h%2 == 0){
                    p.down = new Pixel(img.getRGB(0, h));
                }
                else{
                    p.down = new Pixel(img.getRGB(width-1, h));
                }
                p.down.up = p;
                p = p.down;
            }
            if(h%2 == 0){
                // go right
                for(int w = 1; w < width; w++){
                    p.right = new Pixel(img.getRGB(w, h));
                    p.right.left = p;
                    if(h > 0){
                        p.right.up = p.up.right;
                        p.up.right.down = p.right;
                    }
                    p = p.right;
                }
            }
            else{
                // go left
                for(int w = width-2; w >= 0; w--){
                    p.left = new Pixel(img.getRGB(w, h));
                    p.left.right = p;
                    if(h > 0){
                        p.left.up = p.up.left;
                        p.up.left.down = p.left;
                    }
                    p = p.left;
                }
            }
        }
        return root;
    }
    static public BufferedImage graph2img(Pixel root){
        //get width
        Pixel p = root;
        int width = 1;
        while(p.right != null){
            width++;
            p = p.right;
        }
        //get height
        p = root;
        int height = 1;
        while(p.down != null){
            height++;
            p = p.down;
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Pixel ph = root;
        Pixel pw = root;
        for(int h = 0; h < height; h++){
            if(h > 0){
                ph = ph.down;
            }
            pw = ph;
            for(int w = 0; w < width; w++){
                if(w > 0){
                    pw = pw.right;
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
        if(this.left != null){
            this.e += absGrad(this, this.left);
        }
        else if(this.right != null){
            this.e += absGrad(this, this.right);
        }

        if(this.up != null){
            this.e += absGrad(this, this.up);
        }
        else if(this.down != null){
            this.e += absGrad(this, this.down);
        }
    }
    static public void calEnergy(Pixel root){
        Pixel ph = root;
        Pixel pw = root;
        while(ph != null){
            pw = ph;
            while(pw != null){
                pw.energy();
                //pw.e = 0;
                //if(pw.left != null){
                //    pw.e += absGrad(pw, pw.left);
                //}
                //else if(pw.right != null){
                //    pw.e += absGrad(pw, pw.right);
                //}

                //if(pw.up != null){
                //    pw.e += absGrad(pw, pw.up);
                //}
                //else if(pw.down != null){
                //    pw.e += absGrad(pw, pw.down);
                //}

                if(pw.up != null){
                    pw.dp = pw.up.dp;
                    pw.bt = pw.up;
                    if(pw.up.left != null && pw.up.left.dp < pw.dp){
                        pw.dp = pw.up.left.dp;
                        pw.bt = pw.up.left;
                    }
                    if(pw.up.right != null && pw.up.right.dp < pw.dp){
                        pw.dp = pw.up.right.dp;
                        pw.bt = pw.up.right;
                    }
                    pw.bt.ft = pw;
                }
                pw.dp += pw.e;
                pw = pw.right;
            }
            ph = ph.down;
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
                if(pw.up != null){
                    tdp = pw.up.dp;
                    if(pw.up.left != null && pw.up.left.dp < tdp){
                        tdp = pw.up.left.dp;
                    }
                    if(pw.up.right != null && pw.up.right.dp < tdp){
                        tdp = pw.up.right.dp;
                    }
                }
                tdp += pw.e;
                if(tdp != pw.dp){
                    pw.c = new Color(0,0,255);
                    error++;
                    System.out.println("Error: " + error);
                }
                pw = pw.right;
            }
            ph = ph.down;
        }
    }
    static public void mkColumn(Pixel root){
        Pixel p = root;
        // find the bottom left pixel
        while(p.down != null){
            p = p.down;
        }

        // find the minimum end
        Pixel min = p;
        int i = 0;
        while(p != null){
            if(p.dp < min.dp){
                min = p;
            }
            p = p.right;
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
        while(p.down != null){
            p = p.down;
        }

        // find the minimum end
        Pixel min = p;
        int i = 0;
        while(p != null){
            if(p.dp < min.dp){
                min = p;
            }
            p = p.right;
        }

        // back tracking
        Pixel curr = min;
        Pixel first = min;
        while(curr != null){
            if(curr.bt != null){
                if(curr.up.left == curr.bt){
                    curr.up.down = curr.left;
                    curr.left.up = curr.up;
                }
                else if(curr.up.right == curr.bt){
                    curr.up.down = curr.right;
                    curr.right.up = curr.up;
                }
            }
            if(curr.left != null){
                curr.left.right = curr.right;
                curr.left.chg = 1;
                //curr.left.energy(); // update energy --> do this in updateEnergy()
            }
            if(curr.right != null){
                curr.right.left = curr.left;
                curr.right.chg = 1;
                //curr.right.energy(); // update energy --> do this in updateEnergy()
            }
            first = curr;
            curr = curr.bt;
        }
        if(first == root){
            System.out.println("root changes");
            root = root.right;
        }
        return first;
    }
    static public void updateEnergy(Pixel first){
        // forward tracking
        // update dp
        Queue q = new LinkedList();
        Iterator iterator = q.iterator();
        if(first.left != null){
            first.left.mark = 1;
            q.add(first.left);
        }
        if(first.right != null){
            first.right.mark = 1;
            q.add(first.right);
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
            if(cc.up != null){
                tdp = cc.up.dp;
                tbt = cc.up;
                if(cc.up.left != null && cc.up.left.dp < tdp){
                    tdp = cc.up.left.dp;
                    tbt = cc.up.left;
                }
                if(cc.up.right != null && cc.up.right.dp < tdp){
                    tdp = cc.up.right.dp;
                    tbt = cc.up.right;
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
                if(cc.down != null){
                    if(cc.down.mark == 0){
                        cc.down.mark = 1;
                        q.add(cc.down);
                    }
                    if(cc.down.left != null && cc.down.left.mark == 0){
                        cc.down.left.mark = 1;
                        q.add(cc.down.left);
                    }
                    if(cc.down.right != null && cc.down.right.mark == 0){
                        cc.down.right.mark = 1;
                        q.add(cc.down.right);
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
                File input = new File("view.jpg");
                img = ImageIO.read(input);
                setImg = 1;
            }
            catch (Exception e){}
        }

        Pixel root = Pixel.img2graph(img);
        Pixel.calEnergy(root);
        long time = System.nanoTime();
        Dimension size = frame.getSize();
        int width = (int)size.getWidth();
        for(int i = 0; i < img.getWidth() - width; i++){
            Pixel first = Pixel.rmColumn(root);
            Pixel.updateEnergy(first);
            //Pixel.chkEnergy(root);
        }
        time = System.nanoTime() - time;
        System.out.println("time: " + time/1000000);
        BufferedImage img2 = Pixel.graph2img(root);
        return img2;
    }
    static public void main(String args[]){
        SeamCarving sc = new SeamCarving();
        frame.getContentPane().add(sc);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
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
        Dimension windowSize = new Dimension((int)(size.getWidth()+offset.getX()), (int)(size.getHeight()));
        frame.setSize(windowSize);
    }

    public void mouseMoved(MouseEvent e) {
    }
}
