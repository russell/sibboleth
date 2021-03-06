package grith.sibboleth;

import grisu.jcommons.utils.DefaultGridSecurityProvider;
import grisu.jcommons.utils.HttpProxyManager;
import grisu.jcommons.utils.HttpProxyPanel;
import grisu.jcommons.utils.NewHttpProxyEvent;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.python.core.PyInstance;
import org.python.core.PyObject;

public class ShibLoginDialog extends JDialog implements ShibListener {

	private class LoginAction extends AbstractAction {
		public LoginAction() {
			putValue(NAME, "Login");
			putValue(SHORT_DESCRIPTION, "Logging in");
		}

		public void actionPerformed(ActionEvent e) {

			shibLoginPanel.login();

		}
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {

			EventBus.subscribe(NewHttpProxyEvent.class,
					new EventSubscriber<NewHttpProxyEvent>() {

				public void onEvent(NewHttpProxyEvent arg0) {

					System.out.println("new proxy: "
							+ arg0.getProxyHost());

				}

			});

			ShibLoginDialog dialog = new ShibLoginDialog(
			"https://slcstest.arcs.org.au/SLCS/login");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			HttpProxyManager.setDefaultHttpProxy();

			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private final JPanel contentPanel = new JPanel();

	private final Action action = new LoginAction();

	private ShibLoginPanel shibLoginPanel;

	/**
	 * Create the dialog.
	 */
	public ShibLoginDialog(String url) {

		java.security.Security.addProvider(new DefaultGridSecurityProvider());

		java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",
		"TrustAllCertificates");

		setBounds(100, 100, 529, 431);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			shibLoginPanel = new ShibLoginPanel(url);
			shibLoginPanel.refreshIdpList();
			addWindowListener(shibLoginPanel.getWindowListener());
			contentPanel
			.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			contentPanel.add(shibLoginPanel);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

			JButton okButton = new JButton("OK");
			okButton.setAction(action);
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
			contentPanel.add(buttonPane);

			HttpProxyPanel httpProxyPanel = new HttpProxyPanel();
			httpProxyPanel.setBorder(new TitledBorder(null,
					"Http Proxy settings", TitledBorder.LEADING,
					TitledBorder.TOP, null, null));
			contentPanel.add(httpProxyPanel);

			shibLoginPanel.addShibListener(this);
		}
	}

	public void shibLoginComplete(PyInstance response) {

		Iterable<PyObject> it = response.asIterable();

		StringBuffer responseString = new StringBuffer();

		for (Object element : it) {
			responseString.append(element);
		}

		System.out.println(responseString.toString());
	}

	public void shibLoginFailed(Exception e) {

		JOptionPane.showMessageDialog(ShibLoginDialog.this,
				e.getLocalizedMessage(), "Login error",
				JOptionPane.ERROR_MESSAGE);
	}

	public void shibLoginStarted() {
		// TODO Auto-generated method stub

	}
}
