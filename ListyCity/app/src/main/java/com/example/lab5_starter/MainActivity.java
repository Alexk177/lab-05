package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private CityArrayAdapter cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Firestore setup
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Snapshot listener (keeps list synced with Firestore)
        citiesRef.addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            if (value != null) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    if (name != null && province != null) {
                        cityArrayList.add(new City(name, province));
                    }
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        //add city
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        // Tap city to edit city ( will show Delete button too)
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });
    }

    @Override
    public void addCity(City city) {
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    @Override
    public void updateCity(City city, String newName, String newProvince) {
        String oldName = city.getName();

        city.setName(newName);
        city.setProvince(newProvince);

        // Replace old doc (handles rename)
        citiesRef.document(oldName).delete();
        citiesRef.document(city.getName()).set(city);
    }

    @Override
    public void deleteCity(City city) {
        citiesRef.document(city.getName()).delete();
    }
}
