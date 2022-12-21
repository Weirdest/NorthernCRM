package net.northern.crm;

import net.northern.crm.persistence.services.QuickBooksService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class NorthernCRMApplicationTests {


	private final QuickBooksService quickBooksService;

	@Autowired
	NorthernCRMApplicationTests(QuickBooksService quickBooksService) {
		this.quickBooksService = quickBooksService;
	}

	@Test
	void contextLoads() {
	}

	@SuppressWarnings("UnnecessaryStringEscape")
	@Test
	public void checkSqlStatementPrep() {
		assertEquals("select * from Item where id='5'", QuickBooksService.formatSQL("select * from Item where id=?", 5));
		assertEquals("select * from Item where id='5' and otherThing='OH YEAH'", QuickBooksService.formatSQL("select * from Item where id=? and otherThing=?", 5, "OH YEAH"));
		assertEquals("select * from Item where id='5' and otherthing='\\\'thisguy\\\''", QuickBooksService.formatSQL("select * from Item where id=? and otherthing=?", 5, "'this;guy'"));
	}

/*	@Test
	@Transactional
	public void checkUpdateReceive() {
		Optional<ReceivedEntity> receivedEntity = receivedRepository.findById(8535L);

		receivedEntity.get().setClosed(true);

		ReceivedDTO receivedDTO = new ReceivedDTO(receivedEntity.get());

		receivingApiController.updateReceive(receivedDTO, new Authentication() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return null;
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "test";
			}
		});
	}*/

}
