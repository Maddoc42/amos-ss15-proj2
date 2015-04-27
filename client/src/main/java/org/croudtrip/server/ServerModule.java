package org.croudtrip.server;

import android.content.Context;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import org.croudtrip.DirectionsResource;
import org.croudtrip.R;
import org.croudtrip.UsersResource;
import org.croudtrip.activities.LoginActivity;

import javax.inject.Inject;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class ServerModule implements Module {

	@Override
	public void configure(Binder binder) {
		// nothing to do for now
	}


	@Provides
	@Inject
	public RestAdapter provideRestAdapter(final Context context) {
		// TODO @Vanessa: please replace the static references to LoginActivity once it has been refactored
		return new RestAdapter.Builder()
				.setEndpoint(context.getString(R.string.server_address))
				.setRequestInterceptor(new RequestInterceptor() {
					@Override
					public void intercept(RequestFacade request) {
						if (LoginActivity.isUserLoggedIn(context)) {
							LoginActivity.addAuthorizationHeader(context, request);
						}
					}
				})
				.build();
	}


	@Provides
	@Inject
	public UsersResource provideUsersResource(RestAdapter restAdapter) {
		return restAdapter.create(UsersResource.class);
	}


	@Provides
	@Inject
	public DirectionsResource provideDirectionsResource(RestAdapter restAdapter) {
		return restAdapter.create(DirectionsResource.class);
	}

}
