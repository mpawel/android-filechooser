package edu.pl.lodz.p.course.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TextPreview extends Activity {

	TextPreview mThis;
	File mOpenedFile;
	TextView stats;
	TextView content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.text_preview);
		
		mThis = this;
		mOpenedFile = null;
		
		stats = (TextView) findViewById(R.id.filestats);
		content  = (TextView) findViewById(R.id.filecontent);
		

	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FileChooserActivity.GET_FILE_PATH:
			Intent intent = new Intent(getBaseContext(),
					FileChooserActivity.class);
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras().get("file") != null) {

				mOpenedFile = (File) data.getExtras().get("file");
				String fileContent = "";

				try {

					BufferedReader read = new BufferedReader(
							new InputStreamReader(new FileInputStream(
									mOpenedFile)));
					String line = "";

					while ((line = read.readLine()) != null) {
						fileContent += line + "\n";
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				int wordCount = 0, sentenceCount = 0, charCount = 0;
				charCount = fileContent.replace("\\s+", "").length();

				for (String s : fileContent.replace(",", "").split(".")) {
					if (s.length() > 20)
						sentenceCount++;
				}

				for (String s : fileContent.replace(",", "").split("\\s+")) {
					if (s.length() > 20)
						wordCount++;
				}

				String count = mThis.getString(R.string.count),
						word = mThis.getString(R.string.word),
						character = mThis.getString(R.string.character),
						sentence = mThis.getString(R.string.sentence);

				stats.setText(word + " " + count + ":" + wordCount + "\t" + sentence + " " + count + ":" + sentenceCount + "\t" + character + " " + count + ":" + charCount);
				content.setText(fileContent);

			} else {
				Toast.makeText(this, R.string.none_selected, Toast.LENGTH_SHORT)
						.show();
				mOpenedFile = null;
				content.setText(mThis.getString(R.string.empty));
				stats.setText(mThis.getString(R.string.empty));
			}

		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.textpreview_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.search:
			
			if ( mOpenedFile != null) {
				AlertDialog.Builder alert = new AlertDialog.Builder(mThis);

				alert.setTitle("search");
				alert.setMessage("enter phrase");

				
				// Set an EditText view to get user input
				final EditText input = new EditText(mThis);

				try {

					FileInputStream fis = mThis.getApplication().openFileInput(
							"last_search");
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					input.setText("" + br.readLine());
					
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				

				alert.setView(input);

				alert.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int whichButton) {
								String subtext = input.getText().toString();

								try {

									FileOutputStream fos = mThis.getApplication().openFileOutput("last_search",Context.MODE_PRIVATE);
									PrintStream printStream = new PrintStream(fos);
									printStream.print(subtext);
									printStream.close();

								} catch (FileNotFoundException e) {e.printStackTrace();}

								String fulltext = "" + content.getText();
								fulltext = fulltext.replaceAll(subtext,subtext.toUpperCase());
								content.setText(fulltext,TextView.BufferType.SPANNABLE);
								Spannable str = (Spannable) content.getText();
								subtext = subtext.toUpperCase();

								int i = -1;
								while (i < (fulltext.length() - subtext.length())) {
									i = fulltext.indexOf(subtext, i + 1);
									if (i == -1)
										break;
									str.setSpan(new ForegroundColorSpan(Color.RED), i,i + subtext.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									i += subtext.length();
								}

							}
						});

				alert.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int whichButton) {
								// Canceled.
							}
						});

				alert.show();
			} else {
				Toast.makeText(this, R.string.none_selected, Toast.LENGTH_SHORT).show();
			}
		

			return true;
		case R.id.open:
			
			Intent i = new Intent(getApplicationContext(), FileChooserActivity.class);
			startActivityForResult(i,FileChooserActivity.GET_FILE_PATH);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
