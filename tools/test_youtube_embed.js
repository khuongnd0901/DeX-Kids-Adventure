'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const html = fs.readFileSync('android/src/main/assets/youtube/player.html', 'utf8')
  .replaceAll('__GENERATION__', '9007199254740993').replaceAll('__VIDEO_ID__', 'abcdefghijk')
  .replaceAll('__NONCE__', 'test-channel-nonce').replaceAll('__ORIGIN__', 'https://com.khuongnd.dexkids');
const script = html.match(/<script>([\s\S]*?)<\/script>/)[1];
let listener, inserted=0, options, played=0;
const messages=[];
const context={window:{addEventListener:(type,fn)=>{assert.equal(type,'message');listener=fn;}},
 document:{createElement:()=>({}),head:{appendChild:()=>inserted++}},
 YT:{Player:function(id, config){assert.equal(id,'player');options=config;this.playVideo=()=>played++;}},
 JSON};
vm.createContext(context);vm.runInContext(script,context);
assert.equal(inserted,0,'No player scripts before native channel/gates');
listener({data:'wrong',ports:[{postMessage:()=>assert.fail('untrusted channel')}]});
assert.equal(inserted,0);
listener({data:'test-channel-nonce',ports:[{postMessage:data=>messages.push(JSON.parse(data))}]});
listener({data:'test-channel-nonce',ports:[{}]});
assert.equal(inserted,1,'Exactly one player owner');
vm.runInContext('onYouTubeIframeAPIReady()',context);
assert.equal(options.host,'https://www.youtube-nocookie.com');
assert.equal(options.playerVars.controls,1);
assert.equal(options.playerVars.autoplay,0,'Autoplay requested only after official ready event');
options.events.onReady();options.events.onReady();assert.equal(played,1);
for(const state of [1,2,3,0])options.events.onStateChange({data:state});
options.events.onAutoplayBlocked();options.events.onError();
assert.deepEqual(messages.map(row=>row.event),['playing','paused','buffering','ended','blocked','error']);
assert(messages.every(row=>row.videoId==='abcdefghijk' && row.generation==='9007199254740993'), 'Generation never loses Java long precision in JS');
assert(!html.includes('modestbranding') && !html.includes('pointer-events:none'));
console.log('PASS: actual embed page channel setup, standard player parameters, ready/state/end/error/blocked messages and exact generation; mocked API, no live YouTube');
