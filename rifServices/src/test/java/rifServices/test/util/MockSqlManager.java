package rifServices.test.util;

import java.util.ArrayList;
import java.util.List;

import rifServices.dataStorageLayer.common.BaseSQLManager;
import rifServices.system.RIFServiceStartupOptions;

public class MockSqlManager extends BaseSQLManager {

	private static final List<String> users = new ArrayList<>();

	public MockSqlManager(final RIFServiceStartupOptions options) {

		super(options);
	}

	@Override
	public void login(String user, String password) {

		users.add(user);
	}

	@Override
	public boolean isLoggedIn(String user) {

		return users.contains(user);
	}

	@Override
	public boolean userExists(String user) {

		return isLoggedIn(user);
	}
}
