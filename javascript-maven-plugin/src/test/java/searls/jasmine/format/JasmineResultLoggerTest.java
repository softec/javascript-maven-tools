package searls.jasmine.format;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import searls.jasmine.model.JasmineResult;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JasmineResultLoggerTest {

	@InjectMocks private JasmineResultLogger resultLogger = new JasmineResultLogger();
	@Mock private Log log;
	
	@Test
	public void shouldLogHeader() {
        resultLogger.setBrowser("FF3.6");
		JasmineResult result = new JasmineResult();
		result.setDescription("");
		
		resultLogger.log(result);
		
		verify(log).info(JasmineResultLogger.HEADER.replaceAll("%BROWSER%","FF3.6"));
	}
	
	@Test
	public void shouldLogEmptyResultInTrivialWay() {
        resultLogger.setBrowser("FF3.6");
		String description = "Fake Result";
		JasmineResult result = new JasmineResult();
		result.setDescription(description);
		
		resultLogger.log(result);
		
		verify(log).info("\nResults:\n\n"+description+"\n");
	}
	
}
