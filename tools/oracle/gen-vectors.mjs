import { writeFileSync } from 'node:fs';
import * as PureFunctions from './extract-pure.mjs';

const GOLDEN_DIR = '../../app/src/test/resources/golden/';

async function generateVectors() {
  const testVectors = {};

  // Example for highReturnFlag
  testVectors.highReturnFlag = [
    { name: 'normal_monthly_high', input: ['Earn 5% monthly profit!'], expected: { flag: 'Very high return claimed (5% monthly)', why: '5% a month is about 79% a year, far above what safe options or even good shares pay. Returns like this usually come from new members\' money.' } },
    { name: 'normal_daily_high', input: ['Get 0.5% daily return!'], expected: { flag: 'Very high return claimed (0.5% daily)', why: 'Daily or weekly profit figures are a classic sign of a Ponzi or trading scam.' } },
    { name: 'normal_weekly_high', input: ['1% weekly profit!'], expected: { flag: 'Very high return claimed (1% weekly)', why: 'Daily or weekly profit figures are a classic sign of a Ponzi or trading scam.' } },
    { name: 'normal_monthly_low', input: ['Earn 1% monthly profit.'], expected: null },
    { name: 'empty', input: [''], expected: null },
    { name: 'no_match', input: ['Invest in safe options.'], expected: null },
    { name: 'urdu_monthly', input: ['ماہانہ 5% منافع کمائیں'], expected: { flag: 'Very high return claimed (5%ماہانہ)', why: '5% a month is about 79% a year, far above what safe options or even good shares pay. Returns like this usually come from new members\' money.' } },
    { name: 'roman_urdu_daily', input: ['Rozana 0.5% profit hasil karein'], expected: { flag: 'Very high return claimed (0.5% Rozana)', why: 'Daily or weekly profit figures are a classic sign of a Ponzi or trading scam.' } },
    { name: 'zero_percent', input: ['0% monthly return'], expected: null },
    { name: 'negative_percent', input: ['-5% monthly return'], expected: null },
  ];

  // Example for isQuietTime
  // Requires a settings object and a Date object
  // For simplicity, we'll mock dates or pass specific date strings if the function expects them.
  const mockSettings = {
    quietHoursEnabled: true,
    quietStart: '22:00', // 10 PM
    quietEnd: '06:00'   // 6 AM next day
  };
  const mockSettingsDisabled = { ...mockSettings, quietHoursEnabled: false };
  const mockSettingsSameStartEnd = { ...mockSettings, quietStart: '00:00', quietEnd: '00:00' };


  testVectors.isQuietTime = [
    { name: 'within_quiet_time', input: [mockSettings, new Date('2023-10-26T03:00:00Z')], expected: true }, // 8 AM PKT (3 AM UTC) is within 10 PM to 6 AM
    { name: 'outside_quiet_time', input: [mockSettings, new Date('2023-10-26T10:00:00Z')], expected: false }, // 3 PM PKT (10 AM UTC) is outside
    { name: 'just_before_quiet_start', input: [mockSettings, new Date('2023-10-26T16:59:59Z')], expected: false }, // 9:59 PM PKT (4:59 PM UTC)
    { name: 'just_after_quiet_start', input: [mockSettings, new Date('2023-10-26T17:00:00Z')], expected: true }, // 10:00 PM PKT (5:00 PM UTC)
    { name: 'just_before_quiet_end', input: [mockSettings, new Date('2023-10-26T00:59:59Z')], expected: true }, // 5:59 AM PKT (12:59 AM UTC)
    { name: 'just_after_quiet_end', input: [mockSettings, new Date('2023-10-26T01:00:00Z')], expected: false }, // 6:00 AM PKT (1:00 AM UTC)
    { name: 'disabled_quiet_hours', input: [mockSettingsDisabled, new Date('2023-10-26T03:00:00Z')], expected: false },
    { name: 'same_start_end_time', input: [mockSettingsSameStartEnd, new Date('2023-10-26T03:00:00Z')], expected: false },
    { name: 'empty_settings_no_quiet_hours_enabled', input: [{quietHoursEnabled: false}, new Date()], expected: false},
    { name: 'empty_settings_no_start_end', input: [{quietHoursEnabled: true}, new Date()], expected: false},
  ];

  // Dynamically run each function with its test vectors
  for (const funcName in testVectors) {
    const vectors = testVectors[funcName];
    const results = [];
    for (const vector of vectors) {
      try {
        // Need to correctly map arguments for each function.
        // For simplicity here, assuming arguments are directly spread.
        // This will need careful adjustment for each function's signature.
        const actual = await PureFunctions[funcName](...vector.input);
        results.push({
          name: vector.name,
          input: vector.input.map(arg => {
            if (arg instanceof Date) return arg.toISOString();
            return arg;
          }),
          expected: vector.expected,
          actual: (actual instanceof Date) ? actual.toISOString() : actual,
          passed: JSON.stringify(actual) === JSON.stringify(vector.expected) // Simple comparison
        });
      } catch (e) {
        results.push({
          name: vector.name,
          input: vector.input.map(arg => {
            if (arg instanceof Date) return arg.toISOString();
            return arg;
          }),
          expected: vector.expected,
          actual: `ERROR: ${e.message}`,
          passed: false
        });
      }
    }
    const outputPath = `${GOLDEN_DIR}${funcName}.json`;
    writeFileSync(outputPath, JSON.stringify(results, null, 2), 'utf8');
    console.log(`Generated golden vectors for ${funcName} at ${outputPath}`);
  }
}

generateVectors();
