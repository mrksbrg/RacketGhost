package racketghost.ui;

import racketghost.logic.Ghost;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "racketghost.MESSAGE";
	
	private Ghost ghost;
	private Handler handler;
	private Runnable callDirection;
	//private final Semaphore sem = new Semaphore(1, true);
	private static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ghost = new Ghost(1);
        
        handler = new Handler();
        callDirection = new Runnable() {
        public void run() { 
        	++counter;
        	System.out.println("Starting runnable: " + counter);
        	setText(ghost.nextCorner());

        	if (counter < 10) {
        		handler.postDelayed(callDirection, 2000);
        	}
        	if (counter==10) {
        		setText("Done!");
        	}
	    }
        };
    }
        
    /** Called when the user clicks the Bounce button */
    public void sendMessage(View view) {
        // Do something in response to button

    	Intent intent = new Intent(this, GhostSessionActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);

    }
    
    /** Called when the user clicks the Go button 
     * @throws InterruptedException */
    public void startGhost(View view) throws InterruptedException {
        // Do something in response to button 			
    	setText("Ready?");
   		handler.postDelayed(callDirection, 2000);
    }
    
    public void setText(String corner) {
    	EditText editText = (EditText) findViewById(R.id.edit_message);				
		editText.setText(corner);
    }
}
