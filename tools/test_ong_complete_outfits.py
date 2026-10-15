#!/usr/bin/env python3
"""Synthetic packer tests only; never generates/replaces approved character assets."""
import tempfile
import unittest
from pathlib import Path
from PIL import Image, ImageDraw
from build_ong_complete_outfits import build, regions, STYLES, validate_sheet

class CompleteOngPackerTest(unittest.TestCase):
    def test_optional_absence_and_partial_set_never_modify_outputs(self):
        with tempfile.TemporaryDirectory() as tmp:
            source=Path(tmp)/'source';source.mkdir();output=Path(tmp)/'output'
            self.assertFalse(build(source,output));self.assertFalse(output.exists())
            Image.new('RGBA',(448,672)).save(source/'rose.png')
            with self.assertRaisesRegex(ValueError,'all six'):build(source,output)
            self.assertFalse(output.exists())

    def test_independent_off_center_column_splits_and_short_pet_scale(self):
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'shifted.png';sheet=Image.new('RGBA',(448,672));draw=ImageDraw.Draw(sheet)
            # Left upper pose crosses the nominal y=336 cut, but a safe gap follows.
            draw.rectangle((40,15,180,339),fill=(255,0,0,255))
            draw.rectangle((40,344,180,650),fill=(0,0,255,255))
            draw.rectangle((264,15,404,329),fill=(0,255,0,255))
            # PET stays physically shorter than a standing pose, rather than filling cell.
            draw.rectangle((264,358,404,530),fill=(255,255,0,255));sheet.save(path)
            metadata=[];tiles=validate_sheet(path,metadata);poses=metadata[0]['poses']
            self.assertNotEqual(poses[0]['split_y'],poses[1]['split_y'])
            self.assertEqual([340,344],poses[0]['separating_gap_y_exclusive'])
            self.assertEqual([330,358],poses[1]['separating_gap_y_exclusive'])
            self.assertGreater(poses[0]['alpha128_bbox_source'][3],336)
            self.assertEqual(poses[0]['scale'],poses[3]['scale'])
            self.assertLess(poses[3]['destination_size'][1],poses[0]['destination_size'][1])
            self.assertEqual(4,len(tiles))
            for tile in tiles:
                bounds=tile.getchannel('A').getbbox();self.assertGreater(bounds[0],0);self.assertLess(bounds[3],336)
            # Contamination joining the rows prevents any safe separator, so fail closed.
            draw.rectangle((60,300,65,380),fill=(255,0,0,255));sheet.save(path)
            with self.assertRaisesRegex(ValueError,'separating gap'):validate_sheet(path)

    def test_runtime_draws_ong_once_in_both_full_and_original_modes(self):
        # A source contract complements actual GL screenshot QA: no Ong garment draw may
        # survive in either the companion method or the Ong passenger branch.
        source=(Path(__file__).resolve().parents[1]/'core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java').read_text()
        def method(name):
            start=source.index(name);body=source.index('{',start);depth=1;end=body+1
            while depth:
                depth += (source[end]=='{')-(source[end]=='}');end+=1
            return source[body:end]
        companion=method('void drawOngCompanion(')
        self.assertEqual(1,companion.count('batch.draw('))
        self.assertIn('ongCompleteAvailable ? ongCompleteOutfits',companion)
        self.assertIn('ongStory[active ? storyPose.ordinal()',companion)
        passengers=method('void drawBusPassengers(')
        ong=passengers[passengers.index('if (ong &&'):]
        self.assertEqual(1,ong.count('drawPassenger('))
        self.assertNotIn('drawPassengerOverlay(',ong)
        self.assertNotIn('ongPrincessOutfits',source)
        self.assertNotIn('ongPassengerOutfits',source)
        # Independent Sâu clothes remain the established two-layer path.
        sau=passengers[:passengers.index('if (ong &&')]
        self.assertIn('drawPassengerOverlay(',sau)

    def test_whole_frames_alias_passengers_preserve_sau_and_bound_one_page(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);source=root/'source';source.mkdir();output=root/'output';output.mkdir()
            original=Image.new('RGBA',(2048,2048));atlas=['child-outfits.png','size: 2048, 2048','format: RGBA8888','filter: Linear, Linear','repeat: none']
            expected={}
            for i in range(48):
                name=f'sau_fixture_{i}';x=6+i%14*134;y=6+i//14*198
                tile=Image.new('RGBA',(128,192),(i*5,80,120,123));original.alpha_composite(tile,(x,y));expected[name]=tile.tobytes()
                atlas += [name,'  rotate: false',f'  xy: {x}, {y}','  size: 128, 192','  orig: 128, 192','  offset: 0, 0','  index: -1']
            original.save(output/'child-outfits.png');(output/'child-outfits.atlas').write_text('\n'.join(atlas)+'\n')
            for index,style in enumerate(STYLES):
                sheet=Image.new('RGBA',(448,672));draw=ImageDraw.Draw(sheet)
                for pose in range(4):
                    x=pose%2*224;y=pose//2*336
                    draw.ellipse((x+50,y+15,x+174,y+130),fill=(180,index*30,80,255))
                    draw.rectangle((x+60,y+130,x+164,y+310),fill=(index*30,90,160,255))
                sheet.save(source/f'{style}.png')
            before={p.name:p.read_bytes() for p in source.iterdir()}
            self.assertTrue(build(source,output))
            result=regions((output/'child-outfits.atlas').read_text());self.assertEqual(96,len(result))
            image=Image.open(output/'child-outfits.png');self.assertEqual((2048,2048),image.size)
            mapping={r[0]:r[1:] for r in result}
            for name,x,y,w,h in result:
                self.assertLessEqual(x+w,2048);self.assertLessEqual(y+h,2048)
                if name.startswith('sau_'):self.assertEqual(expected[name],image.crop((x,y,x+w,y+h)).tobytes())
                if '_passenger_' in name:
                    sx,sy,sw,sh=mapping[name.replace('_passenger_','_story_')]
                    self.assertGreaterEqual(x,sx);self.assertGreaterEqual(y,sy)
                    self.assertLessEqual(x+w,sx+sw);self.assertLessEqual(y+h,sy+sh)
            self.assertFalse(any(r[0].startswith('ong_') and not r[0].startswith('ong_complete_') for r in result))
            self.assertEqual(before,{p.name:p.read_bytes() for p in source.iterdir()})
            first=(output/'child-outfits.png').read_bytes();atlas_first=(output/'child-outfits.atlas').read_bytes()
            build(source,output);self.assertEqual(first,(output/'child-outfits.png').read_bytes());self.assertEqual(atlas_first,(output/'child-outfits.atlas').read_bytes())

if __name__=='__main__':unittest.main()
