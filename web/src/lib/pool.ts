import { content } from '../content/types';

/**
 * Port of the IrssaViewModel quiz pools:
 *   generateNewQuizQuestion(): alphabet 50/50 vs numbers
 *   SpeedMatchScreen:         alphabet + numbers 1-20
 */
export function quizPool(): typeof content.alphabetItems {
  const alphabet = Math.random() < 0.5;
  return (alphabet ? content.alphabetItems : content.numberItems) as typeof content.alphabetItems;
}

export function speedPool(): typeof content.alphabetItems {
  return [...content.alphabetItems, ...content.numberItems.slice(0, 21)] as typeof content.alphabetItems;
}
