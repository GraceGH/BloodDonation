package edaebugo.blooddonation_pro;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class seePosting extends Fragment {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    ArrayList<Bloodcard> mData = new ArrayList<Bloodcard>();
    private GridView mGridView = null;
    private BaseAdapterEx mAdapter = null;

    public seePosting(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        process();
        return inflater.inflate(R.layout.fragment_seeposting, container, false);
    }

    public void process(){
        final Context context = getActivity();
        Query myTopPostsQuery = databaseReference.child("posting");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int cnt = 0;
                ArrayList<Bloodcard> tmpData = new ArrayList<Bloodcard>();
                Log.e("seePosting", "Count " + snapshot.getChildrenCount());

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    UploadPage.posting post = postSnapshot.getValue(UploadPage.posting.class);
                    Log.e("seePosting","" + post.getUploader());
                    Bloodcard bloodcard = new Bloodcard();
                    bloodcard.mNumber = "" + post.getHead();
                    bloodcard.mOrder = "" + post.getUploader();
                    tmpData.add(bloodcard);
                    cnt++;
                }
                if(cnt == 0){
                    Bloodcard bloodcard = new Bloodcard();
                    bloodcard.mNumber = "요청글이 존재하지 않습니다!";
                    bloodcard.mOrder = "0";
                    //Log.e("Bloodcardcs","" + bloodcard.mNumber);
                    //Log.e("Bloodcardcs","" + bloodcard.mOrder);
                    tmpData.add(bloodcard);
                }
                Log.e("seePosting","tmpData " + tmpData.size());

                mAdapter = new BaseAdapterEx(context, tmpData);
                if(getView().findViewById(R.id.spgridview) != null) {
                    mGridView = (GridView) getView().findViewById(R.id.spgridview);
                    mGridView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("seePosting","" + databaseError.toString());
                Toast.makeText(getContext().getApplicationContext(), "네트워크를 확인해주세요", Toast.LENGTH_LONG).show();
            }
        });
    }
}
