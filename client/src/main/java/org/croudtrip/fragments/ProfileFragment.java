/*
 * The CroudTrip! application aims at revolutionizing the car-ride-sharing market with its easy,
 * user-friendly and highly automated way of organizing shared Trips. Copyright (C) 2015  Nazeeh Ammari,
 *  Philipp Eichhorn, Ricarda Hohn, Vanessa Lange, Alexander Popp, Frederik Simon, Michael Weber
 * This program is free software: you can redistribute it and/or modify  it under the terms of the GNU
 *  Affero General Public License as published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  You should have received a copy of the GNU Affero General Public License along with this program.
 *    If not, see http://www.gnu.org/licenses/.
 */

package org.croudtrip.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import org.croudtrip.R;
import org.croudtrip.account.AccountManager;
import org.croudtrip.api.VehicleResource;
import org.croudtrip.api.account.User;
import org.croudtrip.api.account.Vehicle;
import org.croudtrip.utils.DataHolder;
import org.croudtrip.utils.DefaultTransformer;
import org.croudtrip.utils.VehiclesListAdapter;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * This fragment shows the user's profile with the data he has entered (e.g. address, phone number
 * etc.). From here he can also edit his profile (will be transferred to fragment EditProfileFragment)
 *
 * @author Vanessa Lange
 */
public class ProfileFragment extends SubscriptionFragment {

    //************************* Variables ***************************//

    @InjectView(R.id.vehicles_list)
    private RecyclerView recyclerView;

    @InjectView(R.id.pb_profile)
    private ProgressWheel progressBar;

    //@InjectView(R.id.tv_profile_image) private ImageView profileImage;
    private ImageView profileImage;
    @Inject
    private VehicleResource vehicleResource;

    private RecyclerView.LayoutManager layoutManager;
    private VehiclesListAdapter adapter;

    //************************* Methods *****************************//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the navigation drawer
        Toolbar toolbar = ((MaterialNavigationDrawer) this.getActivity()).getToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        final Fragment _this = this;
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button addNewVehicle = (Button) view.findViewById(R.id.add_new_vehicle);
        profileImage = (ImageView) view.findViewById(R.id.tv_profile_image);
        // Restore user from SharedPref file
        User user = AccountManager.getLoggedInUser(this.getActivity().getApplicationContext());

        if (user != null) {
            //  Fill in the profile views
            String name = null;
            if (user.getFirstName() != null && user.getLastName() != null) {
                name = user.getFirstName() + " " + user.getLastName();
            } else if (user.getFirstName() != null) {
                name = user.getFirstName();
            } else if (user.getLastName() != null) {
                name = user.getLastName();
            }

            String birthYear = null;
            if (user.getBirthday() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(user.getBirthday());
                birthYear = calendar.get(Calendar.YEAR) + "";
            }

            String gender = null;
            if (user.getIsMale() != null) {
                if (user.getIsMale()) {
                    gender = getString(R.string.profile_male);
                } else if (!user.getIsMale()) {
                    gender = getString(R.string.profile_female);
                }
            }

            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_name), name);
            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_email), user.getEmail());
            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_phone), user.getPhoneNumber());
            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_address), user.getAddress());
            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_gender), gender);
            setTextViewContent((TextView) view.findViewById(R.id.tv_profile_birthyear), birthYear);

            addNewVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ((MaterialNavigationDrawer) getActivity()).getCurrentSection().setTarget(new VehicleInfoFragment());
                    //((MaterialNavigationDrawer) getActivity()).getCurrentSection().setTitle("Add new vehicle");
                    DataHolder.getInstance().setVehicle_id(-1);
                    ((MaterialNavigationDrawer) _this.getActivity()).setFragmentChild(new VehicleInfoFragment(), "Add car");
                }
            });
            // Edit profile button
            FloatingActionButton editProfile = (FloatingActionButton) view.findViewById(R.id.btn_edit_profile);
            editProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MaterialNavigationDrawer) _this.getActivity()).setFragmentChild(new EditProfileFragment(), getString(R.string.profile_edit));
                }
            });


            // download avatar
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null) {
                Timber.i(avatarUrl);
                Picasso.with(getActivity()).load(avatarUrl).error(R.drawable.background_drawer).into(profileImage);
                /*
                Subscription subscription = Observable
                        .defer(new Func0<Observable<Bitmap>>() {
                            @Override
                            public Observable<Bitmap> call() {
                                try {
                                    URL url = new URL(avatarUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    return Observable.just(BitmapFactory.decodeStream(input));
                                } catch (Exception e) {
                                    return Observable.error(e);
                                }
                            }
                        })
                        .compose(new DefaultTransformer<Bitmap>())
                        .subscribe(new Action1<Bitmap>() {
                            @Override
                            public void call(Bitmap avatar) {
                                if (avatar != null) {
                                    ((ImageView) view.findViewById(R.id.tv_profile_image)).setImageBitmap(avatar);
                                } else {
                                    Timber.d("Profile avatar is null");
                                }
                            }
                        }, new CrashCallback(getActivity()));

                subscriptions.add(subscription);
                */
            }
            else
            {
                Timber.i("Avatar url is null");
            }
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = AccountManager.getLoggedInUser(this.getActivity().getApplicationContext());

        // Use a linear layout manager to use the RecyclerView
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new VehiclesListAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);

        //Get a list of user vehicles and add it to the RecyclerView
        Subscription subscription = vehicleResource.getVehicles()
                .compose(new DefaultTransformer<List<Vehicle>>())
                .subscribe(new Action1<List<Vehicle>>() {
                    @Override
                    public void call(List<Vehicle> vehicles) {
                        if (vehicles.size() > 0) {
                            adapter.addElements(vehicles);
                        }
                        //Check if this is the only vehicle the user has
                        if (vehicles.size() == 1) {
                            DataHolder.getInstance().setIsLast(true);
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Response response = ((RetrofitError) throwable).getResponse();
                        if (response != null && response.getStatus() == 401) {  // Not Authorized
                        } else {
                            Timber.e("error" + throwable.getMessage());
                        }
                        Timber.e("Couldn't get data" + throwable.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });

        subscriptions.add(subscription);

        }


    private void setTextViewContent(TextView tv, String content) {
        if (content != null && !content.equals("")) {
            tv.setText(content);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //inflater.inflate(R.menu.menu_main, menu);
    }


}
