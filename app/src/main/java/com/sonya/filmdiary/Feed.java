package com.sonya.filmdiary;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.sonya.filmdiary.databinding.ActivityFeedBinding;
import com.sonya.filmdiary.databinding.ActivityProfileBinding;

import java.util.ArrayList;

public class Feed extends AppCompatActivity {

    private ActivityFeedBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ReviewAdapter reviewAdapter;
    private ArrayList<Review> r = new ArrayList<>();
    String myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getImage();
        binding.toolbar.setTitle("Feed");
        binding.toolbar.inflateMenu(R.menu.profile);
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_item_profile) {

                    startActivity(new Intent(Feed.this, Profile.class).putExtra("myImage", myImage));

                }
                return false;
            }
        });

        getReviews();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getReviews();
                binding.swipe.setRefreshing(false);

            }
        });

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCreateReviewDialog();

            }
        });

    }

    private void getImage(){

        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {

                    if (ds.getValue(User.class).getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

                        myImage = ds.getValue(User.class).getProfilePicture();
                    }

                }
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        }


    private void getReviews(){
        ArrayList<Review> reviews = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviews.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Review review = ds.getValue(Review.class);

                        reviews.add(review);
                    Log.d("REV", review.getTitle());

                }

                binding.recycler.setHasFixedSize(false);
                binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                reviewAdapter = new ReviewAdapter(getApplication(), reviews);
                reviewAdapter.setOnItemClickListener(new ReviewAdapter.ReviewClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {

                        if (reviews.get(position).getAuthor().equals(FirebaseAuth.getInstance().getUid()))

                        showEditReviewDialog(reviews.get(position));

                    }
                });
                binding.recycler.setAdapter(reviewAdapter);
                reviewAdapter.notifyDataSetChanged();

                r = reviews;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showEditReviewDialog(Review review){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.edit_review_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Edit review");

        final EditText title = dialogView.findViewById(R.id.title);
        final EditText text = dialogView.findViewById(R.id.text);
        final Button close = dialogView.findViewById(R.id.close);
        final Button change = dialogView.findViewById(R.id.change);
        final Button delete = dialogView.findViewById(R.id.delete);
        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        title.setText(review.getTitle());
        text.setText(review.getText());
        ratingBar.setRating(review.getMark());


        final AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b.dismiss();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds : snapshot.getChildren()) {


                            if (ds.getValue(Review.class).getTitle().equals(review.getTitle()) && ds.getValue(Review.class).getAuthor().equals(review.getAuthor())) {

                                FirebaseDatabase.getInstance().getReference("review").child(ds.getKey()).removeValue();

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                b.dismiss();


            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String s = title.getText().toString();
                String z = text.getText().toString();

                if (!TextUtils.isEmpty(title.getText().toString().trim()) && !TextUtils.isEmpty(text.getText().toString().trim())) {

                    FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {


                                if (ds.getValue(Review.class).getTitle().equals(review.getTitle()) && ds.getValue(Review.class).getAuthor().equals(review.getAuthor())) {

                                    FirebaseDatabase.getInstance().getReference("review").child(ds.getKey()).setValue(new Review(FirebaseAuth.getInstance().getUid(), s, z, (int) ratingBar.getRating()));

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {

                    title.setError("Enter items");

                }

                b.dismiss();

            }
        });
    }

    private void showCreateReviewDialog(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.create_review_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Create review");

        final EditText title = dialogView.findViewById(R.id.title);
        final EditText text = dialogView.findViewById(R.id.text);
        final Button close = dialogView.findViewById(R.id.close);
        final Button add = dialogView.findViewById(R.id.add);
        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);


        final AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b.dismiss();

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String s = title.getText().toString();
                String z = text.getText().toString();

                if (!TextUtils.isEmpty(title.getText().toString().trim()) && !TextUtils.isEmpty(text.getText().toString().trim())) {

                    FirebaseDatabase.getInstance().getReference("review").push().setValue(new Review(FirebaseAuth.getInstance().getUid(), s, z, (int) ratingBar.getRating()));

                } else {

                    title.setError("Enter items");

                }

                b.dismiss();

            }
        });
    }

}