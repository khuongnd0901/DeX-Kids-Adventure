#!/usr/bin/env python3
"""One owner-authorized developer metadata diagnostic; no playback or child data."""
import argparse
import json
import os
import re
import subprocess
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from prepare_youtube_test_key import read_key

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, fp, code, msg, headers, newurl):
        return None

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env",type=Path,default=Path(".env"))
    parser.add_argument("--apk",type=Path,required=True)
    parser.add_argument("--apksigner",type=Path,required=True)
    parser.add_argument("--video",required=True)
    parser.add_argument("--output",type=Path,required=True)
    args=parser.parse_args()
    report={"scope":"adult developer credential/metadata diagnostic only; no playback", "video_id":args.video}
    try:
        if not re.fullmatch(r"[A-Za-z0-9_-]{11}",args.video):raise ValueError()
        key=read_key(args.env)
        signed=subprocess.run([str(args.apksigner),"verify","--print-certs",str(args.apk)],capture_output=True,text=True,check=True)
        match=re.search(r"certificate SHA-1 digest: ([0-9a-fA-F]+)",signed.stdout)
        if not match:raise ValueError()
        sha1=match.group(1).upper();report["android_signing_sha1"]=sha1
        url="https://www.googleapis.com/youtube/v3/videos?"+urllib.parse.urlencode({"part":"status,contentDetails,snippet","id":args.video,"key":key})
        request=urllib.request.Request(url,headers={"X-Android-Package":"com.khuongnd.dexkids","X-Android-Cert":sha1})
        try:
            response=urllib.request.build_opener(NoRedirect()).open(request,timeout=15)
        except urllib.error.HTTPError as error:
            response=error
        with response:
            report["http_status"]=response.code
            raw=response.read(131073)
            if len(raw)>131072:raise ValueError()
            data=json.loads(raw)
        if report["http_status"]!=200:
            errors=data.get("error",{}).get("errors",[])
            reason=errors[0].get("reason","") if errors else ""
            allowed={"keyInvalid","accessNotConfigured","ipRefererBlocked","forbidden","quotaExceeded","dailyLimitExceeded","rateLimitExceeded","badRequest","keyExpired"}
            report["api_error_reason"]=reason if reason in allowed else "unspecified"
        else:
            items=data.get("items",[])
            report["video_found"]=len(items)==1 and items[0].get("id")==args.video
            if report["video_found"]:
                item=items[0];status=item.get("status",{});details=item.get("contentDetails",{})
                report["made_for_kids"]=status.get("madeForKids") if type(status.get("madeForKids")) is bool else None
                report["embeddable"]=status.get("embeddable") if type(status.get("embeddable")) is bool else None
                privacy=status.get("privacyStatus");report["privacy_status"]=privacy if privacy in {"public","unlisted","private"} else "unknown"
                duration=details.get("duration","")
                report["duration"]=duration if re.fullmatch(r"P[0-9DTHMS.]+",duration) else "unknown"
                report["age_restricted"]=details.get("contentRating",{}).get("ytRating")=="ytAgeRestricted"
                report["has_region_restriction"]=bool(details.get("regionRestriction"))
        report["credential_diagnostic"]="HTTP_200" if report.get("http_status")==200 else "REJECTED"
    except Exception:
        # Never echo raw request URLs, exception strings, environment or response bodies.
        report["credential_diagnostic"]="UNAVAILABLE"
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n")
    print(json.dumps(report,ensure_ascii=False,indent=2))
    return 0 if report.get("credential_diagnostic")=="HTTP_200" else 1

if __name__=="__main__":raise SystemExit(main())
