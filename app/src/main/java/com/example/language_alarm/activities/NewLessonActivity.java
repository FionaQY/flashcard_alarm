package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.adapter.FlashcardAdapter;
import com.example.language_alarm.adapter.InputFlashcardAdapter;
import com.example.language_alarm.models.ActivityResultHelper;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.utils.LessonHandler;
import com.example.language_alarm.utils.PermissionUtils;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.FlashcardViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewLessonActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable headersUpdateRunnable;
    private String searchString = "";
    private MaterialToolbar toolbar = null;
    private Lesson tempLesson = null;
    private List<String> currentHeaders = new ArrayList<>();
    private List<Boolean> foreignIndexes = new ArrayList<>();
    private FlashcardViewModel flashcardViewModel = null;
    private ActivityResultHelper csvPickerHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lesson);


        setupListeners();
        setupToolbar();

        csvPickerHelper = new ActivityResultHelper(this, this::showCsvImportProgress);

        RecyclerView recyclerView = findViewById(R.id.flashcard_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FlashcardAdapter adapter = new FlashcardAdapter(new ArrayList<>(), new ArrayList<>(),
                new FlashcardAdapter.OnFlashcardEditListener() {
                    @Override
                    public void onFlashcardEdit(Flashcard flashcard, int position, View view) {
                        showPopupMenu(flashcard, position, view);
                    }

                    @Override
                    public boolean onFlashcardStar(Flashcard flashcard, int position) {
                        return markFlashcardAsImportant(flashcard, position);
                    }
                });
        recyclerView.setAdapter(adapter);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
        flashcardViewModel.getFlashcards().observe(this, adapter::setFlashcards);
        flashcardViewModel.getHeaders().observe(this, adapter::setHeaders);

        this.tempLesson = getIntent().getParcelableExtra("lesson");
        if (tempLesson == null) {
            this.tempLesson = new Lesson();
        } else {
            // populate stuff at the end or risk null pointer
            populateLessonData();
            findViewById(R.id.practiceButton).setVisibility(View.VISIBLE);
            findViewById(R.id.practiceButton).setOnClickListener(v -> showMemoDialog());
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
        toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "New Lesson", true, this::showExitDialog);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setupListeners() {
        MaterialButton addFlashcardButton = findViewById(R.id.addFlashcardButton);
        addFlashcardButton.setOnClickListener(v -> {
            addFlashcardButton.setVisibility(View.GONE);
            findViewById(R.id.optionsContainer).setVisibility(View.VISIBLE);
        });
        findViewById(R.id.manuallyAddButton).setOnClickListener(v -> startManualFlashcardCreation());
        findViewById(R.id.csvImportButton).setOnClickListener(v -> importFromCsv());
        findViewById(R.id.copyButton).setOnClickListener(v -> showCopyPasteDialog());
        findViewById(R.id.preferencesButton).setOnClickListener(v -> showPreferencesDialog());
        findViewById(R.id.saveLessonButton).setOnClickListener(v -> saveLesson());

        ((TextInputEditText) findViewById(R.id.searchBar).findViewById(R.id.searchEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                searchString = s.toString().trim();
                updateFlashcardListView(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    private void updateFlashcardListView(boolean updateHeaders) {
        if (tempLesson == null) return;

        if (updateHeaders) flashcardViewModel.setHeaders(tempLesson.getHeaders());
        flashcardViewModel.setFlashcards(getFilteredFlashcards());
    }

    private List<Flashcard> getFilteredFlashcards() {
        List<Flashcard> cards = new ArrayList<>();
        if (tempLesson.getFlashcards() == null) {
            return cards;
        }
        List<Flashcard> currCards = tempLesson.getFlashcards();
        for (int i = 0; i < currCards.size(); i++) {
            Flashcard card = currCards.get(i);
            if (searchString.isEmpty() || card.toString().toUpperCase().contains(searchString.toUpperCase())) {
                cards.add(card);
            }
            card.originalIndex = i;
        }
        return cards;
    }

    // Functions to add new flashcards
    private void startManualFlashcardCreation() {
        Flashcard newFlashcard = new Flashcard(new ArrayList<>(this.tempLesson.getHeaders().size()));
        this.tempLesson.getFlashcards().add(newFlashcard);
        showEditFlashcardDialog(newFlashcard, this.tempLesson.getFlashcards().size() - 1);
        Toast.makeText(this, "Flashcard successfully created", Toast.LENGTH_SHORT).show();
    }

    private void importFromCsv() {
        if (PermissionUtils.noStoragePermission(this)) {
            PermissionUtils.requestStoragePermission(this);
            return;
        }

        if (this.currentHeaders == null || currentHeaders.isEmpty()) {
            Toast.makeText(this, "As you have not set the headers in settings, the headers will be derived form the first row of the CSV file", Toast.LENGTH_LONG).show();
        }
        csvPickerHelper.launchCsvPicker();
    }

    private void showCsvImportProgress(Uri uri) {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Importing CSV")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        executor.execute(() -> {
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
                        if (currentHeaders == null || currentHeaders.isEmpty()) {
                            currentHeaders = values;
                            continue;
                        } else {
                            while (currentHeaders.size() < values.size()) {
                                currentHeaders.add("No name");
                            }
                        }
                    }
                    importedCards.add(new Flashcard(values));
                }
                reader.close();

                handler.post(() -> {
                    progressDialog.dismiss();
                    if (!importedCards.isEmpty()) {
                        handleSuccessfulImport(importedCards);
                    } else {
                        Toast.makeText(this,
                                "No valid flashcards found", Toast.LENGTH_SHORT).show();
                    }

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

    private void copyFromGoogleSheet(String pastedText) {
        executor.execute(() -> {
            try {
                String[] linesArray = pastedText.split("\\R");
                List<Flashcard> importedCards = new ArrayList<>();
                for (int lineNum = 0; lineNum < linesArray.length; lineNum++) {
                    String line = linesArray[lineNum].trim();
                    if (line.isEmpty()) continue;

                    List<String> values = parseExcelLine(line);
                    if (lineNum == 0) {
                        if (currentHeaders == null || currentHeaders.isEmpty()) {
                            currentHeaders = values;
                            continue;
                        } else {
                            while (currentHeaders.size() < values.size()) {
                                currentHeaders.add("No name");
                            }
                        }
                    }
                    importedCards.add(new Flashcard(values));
                }

                handler.post(() -> {
                    if (!importedCards.isEmpty()) {
                        handleSuccessfulImport(importedCards);
                    } else {
                        Toast.makeText(this,
                                "No valid flashcards found", Toast.LENGTH_SHORT).show();
                    }

                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this,
                        "Error copying from Google Sheet: " + e.getMessage(),
                        Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void handleSuccessfulImport(List<Flashcard> cards) {
        if (cards.isEmpty()) {
            Toast.makeText(this, "No valid flashcards found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tempLesson == null) {
            tempLesson = new Lesson();
        }
        tempLesson.setHeaders(currentHeaders);
        tempLesson.addFlashcards(cards);
        updateFlashcardListView(true);
        new AlertDialog.Builder(this)
                .setTitle("Import Successful")
                .setMessage(String.format(Locale.US,
                        "Added %d flashcards (Total: %d)",
                        cards.size(),
                        tempLesson.getFlashcards().size()))
                .setPositiveButton("OK", null)
                .show();
    }

    private List<String> parseExcelLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\t') {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }

        values.add(sb.toString().trim());
        return values;
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

    // Dialogs
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
        MaterialButton btnCancel = dialogView.findViewById(R.id.cancelButton);
        MaterialButton btnSave = dialogView.findViewById(R.id.saveButton);

        recyclerEnglish.setLayoutManager(new LinearLayoutManager(this));
        recyclerGerman.setLayoutManager(new LinearLayoutManager(this));

        if (tempLesson != null) {
            switchCapitalization.setChecked(tempLesson.isCaseSensitive());
            switchPunctuation.setChecked(tempLesson.isPunctSensitive());
            editLessonName.setText(tempLesson.getLessonName());
            editHeaders.setText(tempLesson.getHeadersString());
        }

        setupHeaderMapping(recyclerEnglish, recyclerGerman);

        AlertDialog dialog = builder.create();
        dialog.show();

        editHeaders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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
            public void afterTextChanged(Editable s) {
            }
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

    private void showCopyPasteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_copy, null);
        builder.setView(dialogView);

        EditText pasteText = dialogView.findViewById(R.id.pasteText);
        MaterialButton btnPaste = dialogView.findViewById(R.id.pasteButton);
        MaterialButton btnClear = dialogView.findViewById(R.id.clearButton);
        MaterialButton btnCancel = dialogView.findViewById(R.id.cancelButton);
        MaterialButton btnSave = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pasteData = (String) item.getText();
                pasteText.setText(pasteData);
            }

        });
        btnClear.setOnClickListener(v -> pasteText.setText(""));
        btnSave.setOnClickListener(v -> {
            copyFromGoogleSheet(Objects.requireNonNull(pasteText.getText()).toString());
            dialog.dismiss();
        });
    }

    private void showMemoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_memo, null);
        builder.setView(dialogView);

        EditText numOfQns = dialogView.findViewById(R.id.numOfQns);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.nextButton).setOnClickListener(v -> {
            String sTextFromET = numOfQns.getText().toString();
            int nIntFromET = Integer.parseInt(sTextFromET);
            if (nIntFromET <= 0) {
                Toast.makeText(this, "Invalid number of questions", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MemorisationActivity.class);
            intent.putExtra("lessonId", tempLesson.getId());
            intent.putExtra("qnCount", nIntFromET);
            startActivity(intent);
            dialog.dismiss();
        });
    }

    private void showPopupMenu(Flashcard flashcard, int position, View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.popup_menu);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.edit) {
                showEditFlashcardDialog(flashcard, position);
                return true;
            } else if (id == R.id.delete) {
                deleteFlashcard(flashcard, position);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private int getIndex(int position, Flashcard flashcard) {
        return (!searchString.isEmpty() && flashcard.originalIndex != -1) ? flashcard.originalIndex : position;
    }

    private boolean markFlashcardAsImportant(Flashcard flashcard, int position) {
        int index = getIndex(position, flashcard);
        return tempLesson.getFlashcards().get(index).flipImportance();
    }

    private void deleteFlashcard(Flashcard flashcard, int position) {
        int index = getIndex(position, flashcard);
        tempLesson.getFlashcards().remove(index);
        updateFlashcardListView(false);
    }

    private void showEditFlashcardDialog(Flashcard flashcard, int position) {
        int index = getIndex(position, flashcard);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_lesson, null);
        builder.setView(dialogView);

        RecyclerView recyclerView2 = dialogView.findViewById(R.id.values_list);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        InputFlashcardAdapter inputAdapter = new InputFlashcardAdapter(true, recyclerView2);
        recyclerView2.setAdapter(inputAdapter);
        inputAdapter.setLesson(this.tempLesson);
        inputAdapter.setValues(flashcard);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM));

        dialog.show();

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.saveButton).setOnClickListener(v -> {
            List<String> newVals = inputAdapter.getUserAnswers();
            tempLesson.getFlashcards().get(index).setVals(newVals);
            updateFlashcardListView(false);
            dialog.dismiss();
        });
    }

    private void savePreferences(boolean capitalization, boolean punctuation,
                                 String lessonName, String headersString,
                                 RecyclerView germanRecycler) {
        tempLesson.setIsCaseSensitive(capitalization);
        tempLesson.setIsPunctSensitive(punctuation);
        tempLesson.setLessonName(lessonName);
        if (lessonName != null && !lessonName.trim().isEmpty()) {
            TextView titleView = this.toolbar.findViewById(R.id.toolbar_title);
            if (titleView != null) {
                titleView.setText(tempLesson.getLessonName());
            }
        }

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
        flashcardViewModel.setHeaders(tempLesson.getHeaders());
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

        setupPreferencesListeners(englishAdapter, germanAdapter);
    }

    private void setupPreferencesListeners(HeaderAdapter englishAdapter, HeaderAdapter germanAdapter) {
        englishAdapter.setOnItemClickListener((view, position) -> {
            HeaderItem item = englishAdapter.getItems().get(position);
            englishAdapter.getItems().remove(position);
            englishAdapter.notifyItemRemoved(position);

            int insertPosition = findInsertPosition(germanAdapter.getItems(), item.getOriginalIndex());
            germanAdapter.getItems().add(insertPosition, item);
            germanAdapter.notifyItemInserted(insertPosition);
        });

        germanAdapter.setOnItemClickListener((view, position) -> {
            HeaderItem item = germanAdapter.getItems().get(position);
            germanAdapter.getItems().remove(position);
            germanAdapter.notifyItemRemoved(position);

            int insertPosition = findInsertPosition(englishAdapter.getItems(), item.getOriginalIndex());
            englishAdapter.getItems().add(insertPosition, item);
            englishAdapter.notifyItemInserted(insertPosition);
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

    // functions related to the lesson
    private void saveLesson() {
        if (tempLesson.getLessonName() == null || tempLesson.getLessonName().trim().isEmpty()) {
            Toast.makeText(this, "Please set a lesson name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tempLesson.getHeaders().size() < 2) {
            Toast.makeText(this, "Please configure at least 2 headers", Toast.LENGTH_SHORT).show();
            return;
        }

        LessonHandler.saveLesson(this, tempLesson);
        Toast.makeText(this, String.format(Locale.US, "Lesson %s saved", tempLesson.getLessonName()), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void populateLessonData() {
        if (tempLesson.getLessonName() != null && !tempLesson.getLessonName().isEmpty()) {
            TextView titleView = this.toolbar.findViewById(R.id.toolbar_title);
            if (titleView != null) {
                titleView.setText(tempLesson.getLessonName());
            }
        }
        this.currentHeaders = new ArrayList<>(tempLesson.getHeaders());
        this.foreignIndexes = new ArrayList<>(tempLesson.getForeignIndexes());

        updateFlashcardListView(true);
    }

    public void showExitDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Go back? Changes made will not be saved")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.tempLesson != null) {
            populateLessonData();
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
        executor.shutdownNow();
    }

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private static class HeaderItem {
        private final String text;
        private final int originalIndex;

        public HeaderItem(String text, int originalIndex) {
            this.text = text;
            this.originalIndex = originalIndex;
        }

        public String getText() {
            return text;
        }

        public int getOriginalIndex() {
            return originalIndex;
        }
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

        public List<HeaderItem> getItems() {
            return items;
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {
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
