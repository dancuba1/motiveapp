package com.example.calendarapp.Utils;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
public class LocationUtils {
        public static Address getAddressFromString(String locationAddress, Context context) {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses;
            Address addressResult = null;

            try {
                // Attempt to get an address for the given location. Note that the list might be empty.
                addresses = geocoder.getFromLocationName(locationAddress, 1);
                if (!addresses.isEmpty()) {
                    // Use the first address found by the geocoder.
                    addressResult = addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return addressResult;
        }

}
