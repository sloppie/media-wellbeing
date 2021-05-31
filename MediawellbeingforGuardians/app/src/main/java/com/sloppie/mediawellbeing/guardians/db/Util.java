package com.sloppie.mediawellbeing.guardians.db;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.sloppie.mediawellbeing.guardians.adapters.ProfileAdapter;
import com.sloppie.mediawellbeing.guardians.api.model.Guardian;
import com.sloppie.mediawellbeing.guardians.api.model.Profile;
import com.sloppie.mediawellbeing.guardians.db.model.Child;
import com.sloppie.mediawellbeing.guardians.db.model.Parent;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

public class Util {
    public static String generateChildName(String id) {
        String[] tuple = id.split(":");
        String name = "";

        if (tuple.length > 1) {
            name = tuple[tuple.length - 1];
        }

        return name;
    }

    public static String extractGuardianId(String id) {
        String[] tuple = id.split(":");
        String name = "";

        if (tuple.length > 1) {
            name = tuple[0];
        }

        return name;
    }

    public static void setParentCredentials(Context context, Guardian guardian) {
        Parent parent = new Parent(guardian.getId(), guardian.getName(), guardian.getEmail());

        new Thread(() -> AppDb.getInstance(context).parentDao().insert(parent)).start();
    }

    public static void createNewProfile(ProfileAdapter.ProfileManager profileManager, String name) {
        // create the child row in the child table
        new Thread(() -> {
            AppDb appDb = AppDb.getInstance(profileManager.getApplicationContext());
            ParentDao parentDao = appDb.parentDao();
            ChildDao childDao = appDb.childDao();


            List<Parent> parents = parentDao.getAllParents();
            if (parents.size() > 0) {
                Parent parent = parents.get(0);
                String childId = parent.getId() + ":" + name;
                List<Child> existingChildren = childDao.getChild(childId);

                if (existingChildren.size() == 0) {
                    Child newChild = new Child(childId);

                    childDao.insert(newChild);
                    Profile newProfile = Profile.fromChild(newChild);
                    profileManager.updateProfile(newProfile);
                } else {
                    Toast.makeText(profileManager.getApplicationContext(), "Profile already exist", Toast.LENGTH_SHORT);
                }
            }
        }).start();
    }
}
