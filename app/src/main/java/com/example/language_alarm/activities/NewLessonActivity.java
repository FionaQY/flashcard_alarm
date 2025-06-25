package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NewLessonActivity extends AppCompatActivity {

    private MaterialButton btnAddFlashcards, btnManualAdd, btnCsvImport, btnPreferences, btnSaveLesson;
    private LinearLayout optionsContainer;
    private Toolbar toolbar;
    private Lesson tempLesson = null;

    // Preferences dialog views
    private List<String> currentHeaders = new ArrayList<>();
    private List<Integer> englishIndices = new ArrayList<>();
    private List<Integer> germanIndices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_lesson);

        toolbar = findViewById(R.id.toolbar);
        btnAddFlashcards = findViewById(R.id.btnAddFlashcards);
        btnManualAdd = findViewById(R.id.btnManualAdd);
        btnCsvImport = findViewById(R.id.btnCsvImport);
        btnPreferences = findViewById(R.id.btnPreferences);
        btnSaveLesson = findViewById(R.id.btnSaveLesson);
        optionsContainer = findViewById(R.id.optionsContainer);

        setupToolbar();
        setupButtons();

        Lesson lessonToEdit = getIntent().getParcelableExtra("lesson");
        if (lessonToEdit != null) {
            this.tempLesson = lessonToEdit;
            populateLessonData(this.tempLesson);
        }

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.lessons_title);
        }
    }

    private void setupButtons() {
        btnAddFlashcards.setOnClickListener(v -> showAddOptions());
        btnManualAdd.setOnClickListener(v -> startManualFlashcardCreation());
        btnCsvImport.setOnClickListener(v -> importFromCsv());
        btnPreferences.setOnClickListener(v -> showPreferencesDialog());
        btnSaveLesson.setOnClickListener(v -> saveLesson());
    }

    private void showAddOptions() {
        btnAddFlashcards.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void startManualFlashcardCreation() {
        Toast.makeText(this, "Manual flashcard creation", Toast.LENGTH_SHORT).show();
    }

    private void importFromCsv() {
        Toast.makeText(this, "CSV import", Toast.LENGTH_SHORT).show();
    }

    private void showPreferencesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preferences, null);
        builder.setView(dialogView);

        SwitchMaterial switchCapitalization = dialogView.findViewById(R.id.switchCapitalization);
        SwitchMaterial switchPunctuation = dialogView.findViewById(R.id.switchPunctuation);
        EditText editLessonName = dialogView.findViewById(R.id.inputLessonName);
        TextInputEditText editHeaders = dialogView.findViewById(R.id.editHeaders);
        RecyclerView recyclerEnglish = dialogView.findViewById(R.id.recyclerEnglish);
        RecyclerView recyclerGerman = dialogView.findViewById(R.id.recyclerGerman);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelPrefs);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSavePrefs);

        recyclerEnglish.setLayoutManager(new LinearLayoutManager(this));
        recyclerGerman.setLayoutManager(new LinearLayoutManager(this));

        if (tempLesson != null) {
            switchCapitalization.setChecked(tempLesson.isCareAboutCapitalisation());
            switchPunctuation.setChecked(tempLesson.isCareAboutPunctuation());
            editLessonName.setText(tempLesson.getLessonName());
            editHeaders.setText(tempLesson.getHeadersString());
            currentHeaders = new ArrayList<>(tempLesson.getHeaders());
            englishIndices = new ArrayList<>(tempLesson.getEnglishIndexes());
            germanIndices = new ArrayList<>(tempLesson.getGermanIndexes());
        }

        setupHeaderMapping(recyclerEnglish, recyclerGerman);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            savePreferences(
                    switchCapitalization.isChecked(),
                    switchPunctuation.isChecked(),
                    editLessonName.getText().toString(),
                    Objects.requireNonNull(editHeaders.getText()).toString(),
                    recyclerEnglish,
                    recyclerGerman
            );
            dialog.dismiss();
        });
    }

    private void savePreferences(boolean capitalization, boolean punctuation,
                                 String lessonName, String headersString,
                                 RecyclerView englishRecycler, RecyclerView germanRecycler) {
        if (tempLesson == null) {
            tempLesson = new Lesson();
        }

        tempLesson.setCareAboutCapitalisation(capitalization);
        tempLesson.setCareAboutPunctuation(punctuation);
        tempLesson.setLessonName(lessonName);

        List<String> headers = Arrays.asList(headersString.split(","));
        tempLesson.setHeaders(headers);

        // Save the mapped indices
        List<Integer> englishIndices = new ArrayList<>();
        List<Integer> germanIndices = new ArrayList<>();

        HeaderAdapter englishAdapter = (HeaderAdapter) englishRecycler.getAdapter();
        HeaderAdapter germanAdapter = (HeaderAdapter) germanRecycler.getAdapter();

        if (englishAdapter != null && germanAdapter != null) {
            for (HeaderItem germanItem : germanAdapter.getItems()) {
                boolean found = false;
                for (HeaderItem englishItem : englishAdapter.getItems()) {
                    if (germanItem.getOriginalIndex() == englishItem.getOriginalIndex()) {
                        englishIndices.add(englishItem.getOriginalIndex());
                        germanIndices.add(germanAdapter.getItems().indexOf(germanItem));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    englishIndices.add(-1);
                    germanIndices.add(germanAdapter.getItems().indexOf(germanItem));
                }
            }
        }

        tempLesson.setEnglishIndexes(englishIndices);
        tempLesson.setGermanIndexes(germanIndices);
    }

    private void setupHeaderMapping(RecyclerView englishRecycler, RecyclerView germanRecycler) {
        // Prepare data
        List<HeaderItem> englishItems = new ArrayList<>();
        List<HeaderItem> germanItems = new ArrayList<>();

        for (int i = 0; i < currentHeaders.size(); i++) {
            englishItems.add(new HeaderItem(currentHeaders.get(i), i));
        }

        // If we have existing mappings, populate German list
        if (!englishIndices.isEmpty() && !germanIndices.isEmpty()) {
            for (int i = 0; i < englishIndices.size(); i++) {
                int englishIndex = englishIndices.get(i);
                if (englishIndex >= 0 && englishIndex < currentHeaders.size()) {
                    germanItems.add(new HeaderItem(currentHeaders.get(englishIndex), englishIndex));
                }
            }
        }

        // Set up adapters
        HeaderAdapter englishAdapter = new HeaderAdapter(englishItems);
        HeaderAdapter germanAdapter = new HeaderAdapter(germanItems);

        englishRecycler.setAdapter(englishAdapter);
        germanRecycler.setAdapter(germanAdapter);

        // Set up drag and drop
        setupDragAndDrop(englishRecycler, germanRecycler, englishAdapter, germanAdapter);
    }

    private void setupDragAndDrop(RecyclerView englishRecycler, RecyclerView germanRecycler,
                                  HeaderAdapter englishAdapter, HeaderAdapter germanAdapter) {
        // English RecyclerView - only drag to reorder
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAbsoluteAdapterPosition();
                int toPos = target.getAbsoluteAdapterPosition();
                Collections.swap(englishAdapter.getItems(), fromPos, toPos);
                englishAdapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        }).attachToRecyclerView(englishRecycler);

        // German RecyclerView - drag to reorder and swipe to remove
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAbsoluteAdapterPosition();
                int toPos = target.getAbsoluteAdapterPosition();
                Collections.swap(germanAdapter.getItems(), fromPos, toPos);
                germanAdapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                germanAdapter.getItems().remove(position);
                germanAdapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(germanRecycler);

        // Set up long click to transfer items from English to German
        englishAdapter.setOnItemLongClickListener((view, position) -> {
            HeaderItem item = englishAdapter.getItems().get(position);
            germanAdapter.getItems().add(new HeaderItem(item.getText(), item.getOriginalIndex()));
            germanAdapter.notifyItemInserted(germanAdapter.getItemCount() - 1);
            return true;
        });
    }


    private void saveLesson() {
        Toast.makeText(this, "Lesson saved", Toast.LENGTH_SHORT).show();
        finish();
    }


    private Lesson createLesson() {
        if (tempLesson == null) { tempLesson = new Lesson(); }
        String lessonName = "german";
        ArrayList<Flashcard> flashcards = new ArrayList<>();
        boolean punctuation = false;
        boolean capitalisation = false;
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Integer> engInd = new ArrayList<>();
        ArrayList<Integer> deInd = new ArrayList<>();

        return new Lesson(lessonName, flashcards, punctuation, capitalisation, headers, engInd, deInd);
    }

    private void populateLessonData(Lesson lesson) {
        // set values
    }

    public void showExitDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Cancel this lesson? Current input will not be saved")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.tempLesson != null) {
            populateLessonData(this.tempLesson);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        showExitDialog();
        return true;
    }

    private static class HeaderItem {
        private final String text;
        private final int originalIndex;

        public HeaderItem(String text, int originalIndex) {
            this.text = text;
            this.originalIndex = originalIndex;
        }
        public String getText() { return text; }
        public int getOriginalIndex() { return originalIndex; }
    }

    private  interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private static class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder> {
        private List<HeaderItem> items;
        private OnItemLongClickListener longClickListener;
        private OnItemClickListener clickListener;

        public HeaderAdapter(List<HeaderItem> items) {
            this.items = items;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.longClickListener = listener;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HeaderAdapter.HeaderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public List<HeaderItem> getItems() { return items; }

        public void addItem(HeaderItem item) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }

        public void removeItem(int pos) {
            items.remove(pos);
            notifyItemRemoved(pos);
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder{
            private final TextView textView;

            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.header_text);

                itemView.setOnLongClickListener(v -> {
                    if (longClickListener != null) {
                        return longClickListener.onItemLongClick(v, getAbsoluteAdapterPosition());
                    }
                    return false;
                });

                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onItemClick(v, getAbsoluteAdapterPosition());
                    }
                });
            }

            public void bind(HeaderItem item) {
                textView.setText(item.getText());
            }
        }
    }
}
