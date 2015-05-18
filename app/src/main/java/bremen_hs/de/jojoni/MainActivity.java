package bremen_hs.de.jojoni;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    //HalloWelt!!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Der Regeln anzeigen Button wird mit der Funktion verknüpft
        Button buttonShowRules = (Button) findViewById(R.id.btnShowRules);
        buttonShowRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "Show Rules gedrückt");
            }
        });

        // Der Spiel Erstellen Button wird mit der Funktion verknüpft
        Button buttonCreateGame = (Button) findViewById(R.id.btnCreateGame);
        buttonCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Funktion
            }
        });

        // Der Spiel Beitreten Button wird mit der Funktion verknüpft
        Button buttonJoinGame = (Button) findViewById(R.id.btnJoinGame);
        buttonJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Funktion
            }
        });

    }
//Test Commit & Push

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
