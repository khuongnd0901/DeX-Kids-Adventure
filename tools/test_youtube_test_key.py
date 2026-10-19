#!/usr/bin/env python3
"""Exercise provisioning with synthetic keys only; never open the real .env."""
import contextlib
import importlib.util
import io
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location("prepare", Path(__file__).with_name("prepare_youtube_test_key.py"))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class ProvisioningTest(unittest.TestCase):
    def test_explicit_opt_in_and_disabled_cleanup(self):
        with tempfile.TemporaryDirectory() as root:
            root = Path(root); env = root/".env"; output = root/"assets"
            fake = "SYNTHETIC_TEST_ONLY_" + "x"*24
            env.write_text("UNRELATED_SECRET=do-not-copy\nYOUTUBE_KEY='"+fake+"' # test\n")
            module.prepare(env,output,False)
            self.assertFalse(output.exists())
            module.prepare(env,output,True)
            self.assertEqual(json.loads((output/"youtube-test-key.json").read_text()),{"key":fake})
            module.prepare(root/"missing",output,False)
            self.assertFalse((output/"youtube-test-key.json").exists())
    def test_rejects_ambiguous_or_invalid_input_and_deletes_stale_secret(self):
        with tempfile.TemporaryDirectory() as root:
            root=Path(root);env=root/".env";output=root/"assets";output.mkdir()
            for data in ["", "YOUTUBE_KEY=short", "YOUTUBE_KEY="+"x"*30+"\nYOUTUBE_KEY="+"y"*30,
                         'YOUTUBE_KEY="'+"x"*30+'" trailing', "YOUTUBE_KEY=$(do-not-execute)"]:
                env.write_text(data);(output/"youtube-test-key.json").write_text("stale")
                with self.assertRaises(ValueError):module.prepare(env,output,True)
                self.assertFalse((output/"youtube-test-key.json").exists())
    def test_process_output_never_contains_key_or_other_env_values(self):
        with tempfile.TemporaryDirectory() as root:
            root=Path(root);env=root/".env";fake="x"*35
            env.write_text("OTHER=private-other\nexport YOUTUBE_KEY=\""+fake+"\"\n")
            args=[sys.executable,str(Path(__file__).with_name("prepare_youtube_test_key.py")),"--env",str(env),"--output",str(root/"assets"),"--inject"]
            result=subprocess.run(args,capture_output=True,text=True)
            self.assertEqual(result.returncode,0)
            self.assertNotIn(fake,result.stdout+result.stderr)
            self.assertNotIn("private-other",result.stdout+result.stderr)
            env.write_text("YOUTUBE_KEY="+fake+"/invalid")
            result=subprocess.run(args,capture_output=True,text=True)
            self.assertNotEqual(result.returncode,0)
            self.assertNotIn(fake,result.stdout+result.stderr)

if __name__=="__main__":unittest.main()
