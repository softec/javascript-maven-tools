(function() {

  if(!jasmine) {
    throw new Exception("jasmine library does not exist in global namespace!");
  }

  /**
   * TitaniumReporter, by Guilherme Chapiewski - http://guilhermechapiewski.com
   *
   * TitaniumReporter is a Jasmine reporter that outputs spec results to a new
   * window inside your iOS application. It helps you develop Titanium Mobile
   * applications with proper unit testing.
   *
   * More info at http://github.com/guilhermechapiewski/titanium-jasmine
   *
   * Usage:
   *
   * jasmine.getEnv().addReporter(new jasmine.TitaniumReporter());
   * jasmine.getEnv().execute();
   */
  var TitaniumReporter = function() {
    // create Titanium Window and WebView to display results
    var titaniumTestWindow = Titanium.UI.createWindow({
      title : 'Improved Framework Unit Tests',
      backgroundColor : 'white',
      orientationModes : [
        Titanium.UI.PORTRAIT,
        Titanium.UI.UPSIDE_PORTRAIT,
        Titanium.UI.LANDSCAPE_LEFT,
        Titanium.UI.LANDSCAPE_RIGHT
      ],
      modal: true
    });

    var titaniumTestsResultsWebView = Titanium.UI.createWebView({
      html : ('<html><head><style type="text/css">body{font-size:10px;font-family:helvetica;}</style><script>Ti.App.addEventListener("logmessage", function(e){ var div=document.createElement("div");div.innerHTML=e.message;if(e.replace)document.body.replaceChild(div,document.body.lastChild);else document.body.appendChild(div);});</script></head><body></body></html>')
    });

    titaniumTestsResultsWebView.addEventListener('load', function() {
      jasmine.getEnv().execute();
    });

    titaniumTestWindow.add(titaniumTestsResultsWebView);
    titaniumTestWindow.open();

    this.appendTestResults = function(message) {
      Titanium.App.fireEvent('logmessage',{message:message,replace:false});
    };
    this.updateTestResults = function(message) {
      Titanium.App.fireEvent('logmessage',{message:message,replace:true});
    };
  };

  TitaniumReporter.prototype = {
    reportRunnerResults : function(runner) {
      this.lastSuite = null;
      var results = runner.results();
      this.log("<h3>" + results.totalCount + " spec" + (results.totalCount == 1 ? "" : "s" ) + ", " + results.failedCount + " failure" + ((results.failedCount == 1) ? "" : "s") + " in " + ((new Date().getTime() - this.startedAt.getTime()) / 1000) + "s");
    },
    reportRunnerStarting : function(runner) {
      this.startedAt = new Date();
    },
    reportSpecResults : function(spec) {
      var color = '#009900';
      var pass = spec.results().passedCount + ' pass';
      var fail = null;
      if(!spec.results().passed()) {
        color = '#FF0000';
        fail = spec.results().failedCount + ' fail';
      }

      var msg = ' (' + pass;
      if(fail) {
        msg += ', ' + fail
      }
      msg += ')';

      //this.log('[' + spec.suite.description + '] <font color="' + color + '">' + spec.description + '</font><br>');
      this.log('• <font color="' + color + '">' + spec.description + '</font>' + msg + '<br>');

      if(!spec.results().passed()) {
        for(var i = 0; i < spec.results().items_.length; i++) {
          if(!spec.results().items_[i].passed_) {
            this.log('&nbsp;&nbsp;&nbsp;&nbsp;(' + (i + 1) + ') <i>' + spec.results().items_[i].message + '</i><br>');
            if(spec.results().items_[i].expected) {
              this.log('&nbsp;&nbsp;&nbsp;&nbsp;• Expected: "' + spec.results().items_[i].expected + '"<br>');
            }
            this.log('&nbsp;&nbsp;&nbsp;&nbsp;• Actual result: "' + spec.results().items_[i].actual + '"<br>');
            this.log('<br>');
          }
        }
      }
      //Ti.API.debug(JSON.stringify(spec.results()));
    },
    reportSpecStarting : function(spec) {
      if(this.lastSuite !== spec.suite) {
        this.lastSuite = spec.suite;
        this.log('<b>[' + spec.suite.description + '] Tests Suite running... ');
      }
    },
    reportSuiteResults : function(suite) {
      var results = suite.results();

      this.log('<b>[' + suite.description + '] ' + results.passedCount + ' of ' + results.totalCount + ' assertions passed.</b><br><br>');
    },
    log : function(str) {
      if(str == this.lastLog) {
        this.logCount++;
        this.updateTestResults(str + ' (repeated ' + this.logCount + ' times)');
      } else {
        this.lastLog = str;
        this.logCount = 1;
        this.appendTestResults(str);
      }
    }
  };

  // export public
  jasmine.TitaniumReporter = TitaniumReporter;
})();
