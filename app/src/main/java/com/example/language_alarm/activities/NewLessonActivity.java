package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.utils.ToolbarHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class NewLessonActivity extends AppCompatActivity {
    private MaterialButton btnAddFlashcards;
    private LinearLayout optionsContainer;
    private Lesson tempLesson = null;
    private List<String> currentHeaders = new ArrayList<>();
    private List<Boolean> foreignIndexes = new ArrayList<>();
    private ActivityResultLauncher<Intent> csvFilePickerLauncher;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable headersUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_lesson);

        setupViews();
        setupToolbar();

        Lesson lessonToEdit = getIntent().getParcelableExtra("lesson");
        if (lessonToEdit != null) {
            this.tempLesson = lessonToEdit;
            populateLessonData(lessonToEdit);
        }

        // Initialise csv picking launcher
        csvFilePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCsvFileSelection
        );

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "New Lesson", true, this::showExitDialog);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setupViews() {
        btnAddFlashcards = findViewById(R.id.btnAddFlashcards);
        MaterialButton btnManualAdd = findViewById(R.id.btnManualAdd);
        MaterialButton btnCsvImport = findViewById(R.id.btnCsvImport);
        MaterialButton btnPreferences = findViewById(R.id.btnPreferences);
        MaterialButton btnSaveLesson = findViewById(R.id.btnSaveLesson);
        optionsContainer = findViewById(R.id.optionsContainer);

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
        if (this.currentHeaders == null || currentHeaders.isEmpty()) {
            Toast.makeText(this, "As you have not set the headers in settings, the headers will be drived form the first row of the CSV file", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        String[] mimetypes = { "text/csv", "text/comma-seperated-values", "application/csv" };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        csvFilePickerLauncher.launch(intent);
        Toast.makeText(this, "Flashcards imported from CSV file", Toast.LENGTH_SHORT).show();
    }

    private void handleCsvFileSelection(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri != null) {
                showCsvImportProgress(uri);
            }
        }
    }

    private void showCsvImportProgress(Uri uri) {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Importing CSV")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                List<Flashcard> importedCards = new ArrayList<>();
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    if (line.trim().isEmpty()) continue;
                    List<String> values = parseCsvLine(line);
                    if (lineNum == 1) {
                        if (currentHeaders == null) {
                            currentHeaders = values;
                        }
                        continue;
                    }
                    if (values.size() == currentHeaders.size()) {
                        importedCards.add(new Flashcard(values));
                    }
                }
                reader.close();

                handler.post(() -> {
                    progressDialog.dismiss();
                    if (!importedCards.isEmpty()) {
                        handleSuccessfulImport(importedCards);
                    }
                    Toast.makeText(this,
                            "No valid flashcards found", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Error importing CSV: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void handleSuccessfulImport(List<Flashcard> cards) {
        if (tempLesson == null) {
            tempLesson = createLesson();
        }
        tempLesson.AddFlashcards(cards);

        new AlertDialog.Builder(this)
                .setTitle("Import Successful")
                .setMessage(String.format(Locale.US, "Imported %d flashcards", cards.size()))
//                .setPositiveButton("OK", () -> {})
                .show();
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }

        values.add(sb.toString().trim());
        return values;
    }

    private void showPreferencesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preferences, null);
        builder.setView(dialogView);

        SwitchMaterial switchCapitalization = dialogView.findViewById(R.id.switchCapitalization);
        SwitchMaterial switchPunctuation = dialogView.findViewById(R.id.switchPunctuation);
        EditText editLessonName = dialogView.findViewById(R.id.inputLessonName);
        EditText editHeaders = dialogView.findViewById(R.id.editHeaders);
        RecyclerView recyclerEnglish = dialogView.findViewById(R.id.recyclerEnglish);
        RecyclerView recyclerGerman = dialogView.findViewById(R.id.recyclerGerman);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelPrefs);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSavePrefs);

        recyclerEnglish.setLayoutManager(new LinearLayoutManager(this));
        recyclerGerman.setLayoutManager(new LinearLayoutManager(this));

        if (tempLesson != null) {
            switchCapitalization.setChecked(tempLesson.isCaseSensitive());
            switchPunctuation.setChecked(tempLesson.isPunctSensitive());
            editLessonName.setText(tempLesson.getLessonName());
            editHeaders.setText(tempLesson.getHeadersString());
            currentHeaders = new ArrayList<>(tempLesson.getHeaders());
            foreignIndexes = new ArrayList<>(tempLesson.getForeignIndexes());
        }

        setupHeaderMapping(recyclerEnglish, recyclerGerman);

        AlertDialog dialog = builder.create();
        dialog.show();

        editHeaders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (headersUpdateRunnable != null) {
                    handler.removeCallbacks(headersUpdateRunnable);
                }
                headersUpdateRunnable = () -> {
                    String input = s.toString().trim();
                    if (input.contains(",")) {
                        currentHeaders = Arrays.asList(input.split("\\s*,\\s*"));
                        setupHeaderMapping(recyclerEnglish, recyclerGerman);
                    }
                };
                handler.postDelayed(headersUpdateRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            savePreferences(
                    switchCapitalization.isChecked(),
                    switchPunctuation.isChecked(),
                    editLessonName.getText().toString(),
                    Objects.requireNonNull(editHeaders.getText()).toString(),
                    recyclerGerman
            );
            dialog.dismiss();
        });
    }

    private void savePreferences(boolean capitalization, boolean punctuation,
                                 String lessonName, String headersString,
                                 RecyclerView germanRecycler) {
        if (tempLesson == null) {
            tempLesson = new Lesson();
        }

        tempLesson.setIsCaseSensitive(capitalization);
        tempLesson.setIsPunctSensitive(punctuation);
        tempLesson.setLessonName(lessonName);

        List<String> headers = Arrays.asList(headersString.split("\\s*,\\s*"));
        tempLesson.setHeaders(headers);

        List<Boolean> foreignIndices = new ArrayList<>(Collections.nCopies(headers.size(), false));
        HeaderAdapter germanAdapter = (HeaderAdapter) germanRecycler.getAdapter();
        if (germanAdapter != null) {
            for (HeaderItem germanItem : germanAdapter.getItems()) {
                int originalIndex = germanItem.getOriginalIndex();
                if (originalIndex >= 0 && originalIndex < foreignIndices.size()) {
                    foreignIndices.set(germanItem.getOriginalIndex(), true);
                }
            }
        }
        tempLesson.setForeignIndexes(foreignIndices);
    }

    private void setupHeaderMapping(RecyclerView englishRecycler, RecyclerView germanRecycler) {
        List<HeaderItem> englishItems = new ArrayList<>();
        List<HeaderItem> germanItems = new ArrayList<>();

        if (currentHeaders != null && !currentHeaders.isEmpty()) {
            if (foreignIndexes == null || foreignIndexes.size() != currentHeaders.size()) {
                foreignIndexes = new ArrayList<>(Collections.nCopies(currentHeaders.size(), false));
            }
        }

        for (int i = 0; i < Objects.requireNonNull(currentHeaders).size(); i++) {
            HeaderItem item = new HeaderItem(currentHeaders.get(i), i);
            if (foreignIndexes.get(i)) {
                germanItems.add(item);
            } else {
                englishItems.add(item);
            }
        }

        HeaderAdapter englishAdapter = new HeaderAdapter(englishItems);
        HeaderAdapter germanAdapter = new HeaderAdapter(germanItems);

        englishRecycler.setAdapter(englishAdapter);
        germanRecycler.setAdapter(germanAdapter);

        setupDragAndDrop(englishRecycler, germanRecycler, englishAdapter, germanAdapter);
    }

    private void setupDragAndDrop(RecyclerView englishRecycler, RecyclerView germanRecycler,
                                  HeaderAdapter englishAdapter, HeaderAdapter germanAdapter) {
        // English RecyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

        }).attachToRecyclerView(englishRecycler);

        // Foreign Language
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                HeaderItem removedItem = germanAdapter.getItems().get(position);
                germanAdapter.notifyItemRemoved(position);

                int insertPosition = findInsertPosition(englishAdapter.getItems(), removedItem.getOriginalIndex());
                englishAdapter.getItems().add(insertPosition, removedItem);
                englishAdapter.notifyItemInserted(insertPosition);
            }

        }).attachToRecyclerView(germanRecycler);

        englishAdapter.setOnItemClickListener((view, position) -> {
            HeaderItem item = englishAdapter.getItems().get(position);
            englishAdapter.getItems().remove(position);
            englishAdapter.notifyItemRemoved(position);

            int insertPosition = findInsertPosition(germanAdapter.getItems(), item.getOriginalIndex());
            germanAdapter.getItems().add(insertPosition, item);
            germanAdapter.notifyItemInserted(insertPosition);
        });
    }

    private int findInsertPosition(List<HeaderItem> currItems, int originalIndex) {
        for (int i = 0; i < currItems.size(); i++) {
            if (currItems.get(i).getOriginalIndex() > originalIndex) {
                return i;
            }
        }
        return currItems.size();
    }

    private void saveLesson() {
        Toast.makeText(this, "Lesson saved", Toast.LENGTH_SHORT).show();
        finish();
    }


    private Lesson createLesson() {
        if (tempLesson == null) { tempLesson = new Lesson(); }
        String lessonName = tempLesson.getLessonName();
        List<Flashcard> flashcards = new ArrayList<>(); // TODO: get flashcards
        boolean punctuation = tempLesson.isPunctSensitive();
        boolean capitalisation = tempLesson.isCaseSensitive();
        List<String> headers = tempLesson.getHeaders();
        List<Boolean> foreignIndexes = tempLesson.getForeignIndexes();

        return new Lesson(lessonName, flashcards, punctuation, capitalisation, headers, foreignIndexes);
    }

    private void populateLessonData(Lesson lesson) {
        // TODO: set values
        System.out.println(lesson.toString());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && headersUpdateRunnable != null) {
            handler.removeCallbacks(headersUpdateRunnable);
        }
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

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private static class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder> {
        private final List<HeaderItem> items;
        private OnItemClickListener clickListener;

        public HeaderAdapter(List<HeaderItem> items) {
            this.items = items;
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

        public class HeaderViewHolder extends RecyclerView.ViewHolder{
            private final TextView textView;

            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.header_text);

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
