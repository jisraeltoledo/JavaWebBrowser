/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webbrowser;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

// The Simple Web Browser.
public class WebBrowser extends JFrame
        implements HyperlinkListener {

    // These are the buttons for iterating through the page list.
    private JButton backButton, forwardButton;

    // Page location text field.
    private JTextField locationTextField;
    private JTextField durationTextField;

    // Editor pane for displaying pages.
    private JEditorPane displayEditorPane;

    // Browser's list of pages that have been visited.
    private ArrayList pageList = new ArrayList();

    // usuario y contraseña
    private JTextField userTextField;
    private JTextField passTextField;
    private JButton loginButton;

    WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
    HtmlPage currentPage;
    CookieManager cookieMan = new CookieManager();
    javax.swing.Timer timer;
    java.util.List <HtmlAnchor> links = new ArrayList<>();
    // Constructor for Mini Web Browser.

    public WebBrowser() {
        // Set application title.
        super("Mini Browser");

        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);

        // Set window size.
        setSize(640, 480);

        // Handle closing events.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });

        userTextField = new JTextField(15);
        passTextField = new JTextField(15);
        loginButton = new JButton("Login");

        userTextField.setText("DL14TOAJ02027");
        passTextField.setText("7meCPx8O0");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new Label("Usuario: "));
        bottomPanel.add(userTextField);
        bottomPanel.add(new Label("Password: "));
        bottomPanel.add(passTextField);
        bottomPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // Set up file menu.
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit",
                KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Set up button panel.
        JPanel buttonPanel = new JPanel();

        durationTextField = new JTextField(10);
        durationTextField.setText("10");

        buttonPanel.add(new Label ("Duración de la transición (segundos):"));
        buttonPanel.add(durationTextField);
        locationTextField = new JTextField("https://unadmexico.blackboard.com/webapps/login/?action=login");

        // Set up page display.
        displayEditorPane = new JEditorPane();
//        displayEditorPane.setContentType("text/html");
//        displayEditorPane.setEditable(false);
        displayEditorPane.addHyperlinkListener(this);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(displayEditorPane),
                BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    public void login() {
        try {

            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            // Get the first page
            final HtmlPage page1
                    = webClient.getPage(this.locationTextField.getText());

            // Get the form that we are dealing with and within that form, 
            // find the submit button and the field that we want to change.
            final HtmlForm form = page1.getFormByName("login");

            final HtmlSubmitInput button = form.getInputByName("login");
            final HtmlTextInput textField = form.getInputByName("user_id");
            final HtmlPasswordInput passField = form.getInputByName("password");

            // Change the value of the text field
            textField.setValueAttribute(userTextField.getText());
            passField.setValueAttribute(passTextField.getText());

            // Now submit the form by clicking the button and get back the second page.
            currentPage = button.click();
            displayEditorPane.setText(currentPage.asText());

            timer = new javax.swing.Timer(new Integer(durationTextField.getText()) * 1000, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    randomNavigate();
                }
            });
            timer.setInitialDelay(1 * 1000);
            timer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void randomNavigate() {
        links.addAll(currentPage.getAnchors());
        if (links.isEmpty()){
            login ();            
            return;
        }
        Collections.shuffle(links);
        try {
            currentPage = links.get(0).click();
            displayEditorPane.setText(currentPage.asText());            
        } catch (IOException ex) {
            Logger.getLogger(WebBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Exit this program.
    private void actionExit() {
        System.exit(0);
    }

    // Go back to the page viewed before the current page.
    private void actionBack() {
        URL currentUrl = displayEditorPane.getPage();

        int pageIndex = pageList.indexOf(currentUrl.toString());
        try {
            showPage(
                    new URL((String) pageList.get(pageIndex - 1)), false);
        } catch (Exception e) {
        }
    }

    // Go forward to the page viewed after the current page.
    private void actionForward() {
        URL currentUrl = displayEditorPane.getPage();
        int pageIndex = pageList.indexOf(currentUrl.toString());
        try {
            showPage(
                    new URL((String) pageList.get(pageIndex + 1)), false);
        } catch (Exception e) {
        }
    }

    // Load and show the page specified in the location text field.
    private void actionGo() {
        URL verifiedUrl = verifyUrl(locationTextField.getText());
        System.out.println("actionGo: " + verifiedUrl);
        if (verifiedUrl != null) {
            showPage(verifiedUrl, true);
        } else {
            showError("Invalid URL");
        }
    }

    // Show dialog box with error message.
    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Verify URL format.
    private URL verifyUrl(String url) {
        // Only allow HTTP URLs.
//        if (!url.toLowerCase().startsWith("http://"))
//            return null;

        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return verifiedUrl;
    }

    /* Show the specified page and add it to
     the page list if specified. */
    private void showPage(URL pageUrl, boolean addToList) {
        // Show hour glass cursor while crawling is under way.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            // Get URL of page currently being displayed.
            URL currentUrl = displayEditorPane.getPage();

            // Load and display specified page.
            displayEditorPane.setPage(pageUrl);

            // Get URL of new page being displayed.
            URL newUrl = displayEditorPane.getPage();
            System.out.println(newUrl);
            // Add page to list if specified.
            if (addToList) {
                int listSize = pageList.size();
                if (listSize > 0) {
                    int pageIndex
                            = pageList.indexOf(currentUrl.toString());
                    if (pageIndex < listSize - 1) {
                        for (int i = listSize - 1; i > pageIndex; i--) {
                            pageList.remove(i);
                        }
                    }
                }
                if (newUrl != null) {
                    pageList.add(newUrl.toString());
                }
            }

            if (newUrl != null) {
                // Update location text field with URL of current page.
                locationTextField.setText(newUrl.toString());
            }
            // Update buttons based on the page being displayed.
            updateButtons();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unable to load page");
        } finally {
            // Return to default cursor.
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /* Update back and forward buttons based on
     the page being displayed. */
    private void updateButtons() {
        if (pageList.size() < 2) {
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
        } else {
            URL currentUrl = displayEditorPane.getPage();
            int pageIndex = pageList.indexOf(currentUrl.toString());
            backButton.setEnabled(pageIndex > 0);
            forwardButton.setEnabled(
                    pageIndex < (pageList.size() - 1));
        }
    }

    // Handle hyperlink's being clicked.
    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
            if (event instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent linkEvent
                        = (HTMLFrameHyperlinkEvent) event;
                HTMLDocument document
                        = (HTMLDocument) displayEditorPane.getDocument();
                document.processHTMLFrameHyperlinkEvent(linkEvent);
            } else {
                showPage(event.getURL(), true);
            }
        }
    }

    // Run the Mini Browser.
    public static void main(String[] args) {
        WebBrowser browser = new WebBrowser();
        browser.setVisible(true);
    }
}
