package ua.moyo.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.vaadin.testbench.TestBenchTestCase;
import org.jawin.COMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnector;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import ua.moyo.rabbitmq.view.MoYoHomeView;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests  {

	@Test
	public void contextLoads() {





	}

	@Test
	public void connectDB(){

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		try {
			Connection connection = factory.newConnection();
			Channel channel = MoYo.connection.createChannel();
			OdinesComConnector odinesComConnector = OdinesComConnector.getConnector();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (COMException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

	}

}
