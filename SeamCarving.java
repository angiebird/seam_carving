import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Pixel extends Color {
    Pixel up;
    Pixel down;
    Pixel left;
    Pixel right;

    public Pixel(){
        super(0);
    }
    public Pixel(int rgb){
        super(rgb);
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
                img.setRGB(w, h, pw.getRGB());
            }
        }
        return img;
    }
}

class SeamCarving extends JPanel{
    public void paint(Graphics g) {
        BufferedImage img = createImage();
        g.drawImage(img, 0,0,this);
    }
    private BufferedImage createImage(){
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        try{
            File input = new File("view.jpg");
            img = ImageIO.read(input);
        }
        catch (Exception e){}

        Pixel root = Pixel.img2graph(img);
        BufferedImage img2 = Pixel.graph2img(root);
        return img2;
    }
    static public void main(String args[]){
        JFrame frame = new JFrame();
        frame.getContentPane().add(new SeamCarving());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setVisible(true);
    }
}
