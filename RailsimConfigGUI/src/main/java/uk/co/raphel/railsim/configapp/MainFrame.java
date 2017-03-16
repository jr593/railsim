package uk.co.raphel.railsim.configapp;/**
 * Created by johnr on 19/02/2017.
 */

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {
    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1200, 800));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        setVisible(true);
        setState(Frame.NORMAL);
    }
}