-- ============================================================
-- IELTS Seed Data: 2 bài test mẫu hoàn chỉnh
-- Test 1: Reading (3 sections, 13+13+14 = 40 questions)
-- Test 2: Listening (4 parts, 10+10+10+10 = 40 questions)
-- ============================================================

-- ████████████████████████████████████████████████████████████
-- TEST 1: IELTS ACADEMIC READING TEST
-- ████████████████████████████████████████████████████████████

INSERT INTO ielts_tests (id, title, skill, time_limit_minutes, difficulty, is_published)
VALUES ('a1000000-0000-0000-0000-000000000001', 'IELTS Academic Reading Test 1', 'READING', 60, 'MEDIUM', true);

-- ═══════════════════════════════════════════════════
-- SECTION 1: "The Rise of Urban Farming" (13 questions)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, instructions)
VALUES ('b1000000-0000-0000-0000-000000000001',
        'a1000000-0000-0000-0000-000000000001', 1,
        'Section 1', 'Read the passage below and answer Questions 1-13.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c1000000-0000-0000-0000-000000000001',
        'b1000000-0000-0000-0000-000000000001', 1,
        'The Rise of Urban Farming',
        'Urban farming has experienced remarkable growth in recent decades, transforming city landscapes and challenging traditional notions of agriculture. Once considered a niche hobby, growing food in urban environments has become a serious endeavor embraced by individuals, communities, and even corporations.

The concept of urban agriculture is not entirely new. During World War II, "Victory Gardens" were planted in backyards, vacant lots, and public parks across the United States and the United Kingdom. These gardens produced up to 40 percent of all vegetables consumed in the US during the war years, demonstrating the potential of urban food production on a massive scale.

Modern urban farming takes many forms. Rooftop gardens have become increasingly popular in dense metropolitan areas like New York, Tokyo, and Singapore. These elevated growing spaces make use of otherwise wasted area, providing fresh produce while also offering environmental benefits such as building insulation, stormwater management, and urban heat island reduction.

Vertical farming represents perhaps the most technologically advanced form of urban agriculture. These indoor facilities use hydroponic or aeroponic systems to grow crops in stacked layers, often without soil or natural sunlight. Proponents argue that vertical farms can produce significantly higher yields per square meter compared to traditional farming, while using up to 95 percent less water. However, critics point to the high energy costs associated with artificial lighting and climate control systems.

Community gardens serve a different but equally important role. These shared growing spaces bring neighbors together, strengthen social bonds, and provide access to fresh food in areas that might otherwise be food deserts. Research has shown that community gardeners consume more fruits and vegetables than their non-gardening neighbors and report higher levels of physical activity and mental well-being.

Despite its benefits, urban farming faces significant challenges. Land in cities is expensive, and the pressure for housing and commercial development often takes priority. Soil contamination from previous industrial use is another concern that must be addressed through soil testing and remediation. Additionally, urban farmers must navigate complex zoning regulations that were not designed with agriculture in mind.

The economic viability of urban farming remains debated among experts. While some operations, particularly those focused on high-value crops like microgreens, herbs, and specialty lettuces, have proven profitable, others struggle to compete with the economies of scale enjoyed by rural agriculture. Government subsidies and policy support could play a crucial role in determining the long-term sustainability of urban farming initiatives.');

-- Questions 1-5: TRUE / FALSE / NOT GIVEN
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 1,
 'TRUE_FALSE_NOT_GIVEN',
 'Urban farming is a completely modern invention with no historical precedent.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["FALSE"]',
 'Paragraph 2 mentions Victory Gardens during WWII, showing urban farming has historical precedent.'),

('d1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 2,
 'TRUE_FALSE_NOT_GIVEN',
 'Victory Gardens produced up to 40 percent of vegetables consumed in the US during World War II.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["TRUE"]',
 'Paragraph 2 directly states this fact.'),

('d1000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000001', 3,
 'TRUE_FALSE_NOT_GIVEN',
 'Vertical farms always use natural sunlight to grow crops.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["FALSE"]',
 'Paragraph 4 states vertical farms often grow crops "without soil or natural sunlight."'),

('d1000000-0000-0000-0000-000000000004', 'c1000000-0000-0000-0000-000000000001', 4,
 'TRUE_FALSE_NOT_GIVEN',
 'Community gardeners tend to eat more fruits and vegetables than non-gardeners.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["TRUE"]',
 'Paragraph 5 states research shows community gardeners consume more fruits and vegetables.'),

('d1000000-0000-0000-0000-000000000005', 'c1000000-0000-0000-0000-000000000001', 5,
 'TRUE_FALSE_NOT_GIVEN',
 'The majority of urban farms in the US are operated by large corporations.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["NOT GIVEN"]',
 'The passage does not provide information about who operates the majority of urban farms.');

-- Questions 6-9: MULTIPLE CHOICE
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000006', 'c1000000-0000-0000-0000-000000000001', 6,
 'MULTIPLE_CHOICE',
 'According to the passage, rooftop gardens are particularly popular in',
 '["A. rural areas with large open spaces", "B. dense metropolitan areas", "C. suburban neighborhoods", "D. coastal regions"]',
 '["B"]',
 'Paragraph 3 mentions "dense metropolitan areas like New York, Tokyo, and Singapore."'),

('d1000000-0000-0000-0000-000000000007', 'c1000000-0000-0000-0000-000000000001', 7,
 'MULTIPLE_CHOICE',
 'What is a major criticism of vertical farming?',
 '["A. It produces low yields", "B. It requires too much water", "C. It has high energy costs", "D. It cannot grow leafy vegetables"]',
 '["C"]',
 'Paragraph 4 mentions "critics point to the high energy costs."'),

('d1000000-0000-0000-0000-000000000008', 'c1000000-0000-0000-0000-000000000001', 8,
 'MULTIPLE_CHOICE',
 'Which of the following is NOT mentioned as a challenge for urban farming?',
 '["A. Expensive land", "B. Soil contamination", "C. Lack of skilled workers", "D. Complex zoning regulations"]',
 '["C"]',
 'Paragraph 6 mentions A, B, and D. Lack of skilled workers is not discussed.'),

('d1000000-0000-0000-0000-000000000009', 'c1000000-0000-0000-0000-000000000001', 9,
 'MULTIPLE_CHOICE',
 'The passage suggests that government involvement in urban farming would',
 '["A. definitely solve all challenges", "B. have no significant impact", "C. be potentially important for sustainability", "D. only benefit large corporations"]',
 '["C"]',
 'Paragraph 7 states that policy support "could play a crucial role in determining long-term sustainability."');

-- Questions 10-13: SUMMARY COMPLETION
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000010', 'c1000000-0000-0000-0000-000000000001', 10,
 'SUMMARY_COMPLETION',
 'Vertical farming uses _____ or aeroponic systems to grow crops.',
 '[]',
 '["hydroponic"]',
 'Paragraph 4: "use hydroponic or aeroponic systems."'),

('d1000000-0000-0000-0000-000000000011', 'c1000000-0000-0000-0000-000000000001', 11,
 'SUMMARY_COMPLETION',
 'Vertical farms can use up to _____ percent less water than traditional farming.',
 '[]',
 '["95"]',
 'Paragraph 4: "using up to 95 percent less water."'),

('d1000000-0000-0000-0000-000000000012', 'c1000000-0000-0000-0000-000000000001', 12,
 'SUMMARY_COMPLETION',
 'Community gardens help provide fresh food in areas known as food _____.',
 '[]',
 '["deserts"]',
 'Paragraph 5: "food deserts."'),

('d1000000-0000-0000-0000-000000000013', 'c1000000-0000-0000-0000-000000000001', 13,
 'SUMMARY_COMPLETION',
 'Profitable urban farms often focus on high-value crops such as microgreens, herbs, and specialty _____.',
 '[]',
 '["lettuces"]',
 'Paragraph 7: "microgreens, herbs, and specialty lettuces."');

-- ═══════════════════════════════════════════════════
-- SECTION 2: "Sleep and Memory" (13 questions)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, instructions)
VALUES ('b1000000-0000-0000-0000-000000000002',
        'a1000000-0000-0000-0000-000000000001', 2,
        'Section 2', 'Read the passage below and answer Questions 14-26.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c1000000-0000-0000-0000-000000000002',
        'b1000000-0000-0000-0000-000000000002', 1,
        'Sleep and Memory Consolidation',
        'A. The relationship between sleep and memory has fascinated scientists for over a century. Early researchers observed that people who slept after learning new information retained it better than those who stayed awake. Modern neuroscience has since revealed the complex mechanisms by which sleep contributes to memory formation, consolidation, and integration.

B. Memory consolidation—the process by which newly acquired, fragile memories are transformed into stable, long-term representations—occurs predominantly during sleep. This process involves the reactivation of neural patterns that were active during the original learning experience. Using advanced brain imaging techniques, researchers have demonstrated that the same areas of the brain that fire during daytime learning become active again during subsequent sleep periods.

C. Sleep can be broadly divided into two main stages: non-rapid eye movement (NREM) sleep and rapid eye movement (REM) sleep. NREM sleep is further subdivided into three stages, with the deepest stage (N3) characterized by slow-wave oscillations. Research suggests that different types of memories benefit from different sleep stages. Declarative memories—facts and events that can be consciously recalled—appear to be consolidated primarily during NREM slow-wave sleep, while procedural memories—skills and habits—seem to benefit more from REM sleep.

D. One influential theory proposes that during slow-wave sleep, memories are gradually transferred from the hippocampus, where they are initially stored, to the neocortex, where they become part of our long-term knowledge base. This "systems consolidation" theory suggests that sleep provides the optimal conditions for this transfer by reducing interference from new incoming information and allowing the brain to focus on strengthening existing memory traces.

E. The importance of sleep for memory is further demonstrated by studies of sleep deprivation. Participants who are deprived of sleep for even a single night show significant impairments in their ability to form new memories. The hippocampus, which is crucial for encoding new declarative memories, appears to be particularly vulnerable to the effects of sleep loss. Brain imaging studies have shown reduced hippocampal activity in sleep-deprived individuals during learning tasks.

F. Interestingly, not all memories are treated equally during sleep consolidation. Research indicates that the brain selectively consolidates memories that are deemed to be of future relevance. In experimental studies, participants who were told that they would be tested on learned material the following day showed greater sleep-dependent memory improvements compared to those who were not expecting a test, even though both groups learned the same material.

G. The practical implications of these findings are significant. Students who study before sleep and get a full night''s rest are likely to retain more information than those who pull all-night study sessions. Shift workers and individuals with sleep disorders may be at a disadvantage when it comes to learning and memory formation. Understanding the relationship between sleep and memory could also lead to new therapeutic approaches for memory-related disorders such as Alzheimer''s disease.');

-- Questions 14-19: MATCHING HEADINGS
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000014', 'c1000000-0000-0000-0000-000000000002', 14,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph B.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["ii"]',
 'Paragraph B describes how neural patterns are reactivated during sleep for memory consolidation.'),

('d1000000-0000-0000-0000-000000000015', 'c1000000-0000-0000-0000-000000000002', 15,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph C.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["i"]',
 'Paragraph C describes NREM and REM sleep stages and their subdivisions.'),

('d1000000-0000-0000-0000-000000000016', 'c1000000-0000-0000-0000-000000000002', 16,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph D.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["v"]',
 'Paragraph D describes the transfer of memories from hippocampus to neocortex during sleep.'),

('d1000000-0000-0000-0000-000000000017', 'c1000000-0000-0000-0000-000000000002', 17,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph E.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["vi"]',
 'Paragraph E discusses how sleep deprivation impairs memory formation.'),

('d1000000-0000-0000-0000-000000000018', 'c1000000-0000-0000-0000-000000000002', 18,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph F.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["iii"]',
 'Paragraph F describes how the brain selectively consolidates relevant memories.'),

('d1000000-0000-0000-0000-000000000019', 'c1000000-0000-0000-0000-000000000002', 19,
 'MATCHING_HEADINGS',
 'Choose the correct heading for Paragraph G.',
 '["i. Sleep stages explained", "ii. How memories are reactivated during sleep", "iii. The selective nature of memory consolidation", "iv. Early theories about sleep", "v. The hippocampus-to-neocortex transfer", "vi. Effects of sleep loss on memory", "vii. Real-world applications"]',
 '["vii"]',
 'Paragraph G discusses practical implications for students, shift workers, and therapy.');

-- Questions 20-22: MULTIPLE CHOICE
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000020', 'c1000000-0000-0000-0000-000000000002', 20,
 'MULTIPLE_CHOICE',
 'According to the passage, declarative memories are primarily consolidated during',
 '["A. REM sleep", "B. light sleep", "C. NREM slow-wave sleep", "D. waking hours"]',
 '["C"]',
 'Paragraph C: "Declarative memories... appear to be consolidated primarily during NREM slow-wave sleep."'),

('d1000000-0000-0000-0000-000000000021', 'c1000000-0000-0000-0000-000000000002', 21,
 'MULTIPLE_CHOICE',
 'The "systems consolidation" theory suggests that during slow-wave sleep, memories move from',
 '["A. the neocortex to the hippocampus", "B. the hippocampus to the neocortex", "C. REM to NREM stages", "D. short-term to working memory"]',
 '["B"]',
 'Paragraph D: "memories are gradually transferred from the hippocampus... to the neocortex."'),

('d1000000-0000-0000-0000-000000000022', 'c1000000-0000-0000-0000-000000000002', 22,
 'MULTIPLE_CHOICE',
 'What did the experimental study in Paragraph F reveal?',
 '["A. All memories are consolidated equally during sleep", "B. Expecting a test leads to better sleep-dependent memory", "C. Sleep has no effect on memory consolidation", "D. Only procedural memories are consolidated during sleep"]',
 '["B"]',
 'Paragraph F: participants expecting a test showed greater memory improvements.');

-- Questions 23-26: YES / NO / NOT GIVEN
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000023', 'c1000000-0000-0000-0000-000000000002', 23,
 'YES_NO_NOT_GIVEN',
 'Scientists have been interested in the connection between sleep and memory for more than 100 years.',
 '["YES", "NO", "NOT GIVEN"]',
 '["YES"]',
 'Paragraph A: "has fascinated scientists for over a century."'),

('d1000000-0000-0000-0000-000000000024', 'c1000000-0000-0000-0000-000000000002', 24,
 'YES_NO_NOT_GIVEN',
 'Brain imaging has shown increased hippocampal activity in sleep-deprived people during learning.',
 '["YES", "NO", "NOT GIVEN"]',
 '["NO"]',
 'Paragraph E states "reduced hippocampal activity in sleep-deprived individuals," not increased.'),

('d1000000-0000-0000-0000-000000000025', 'c1000000-0000-0000-0000-000000000002', 25,
 'YES_NO_NOT_GIVEN',
 'Alzheimer''s disease is caused primarily by lack of sleep.',
 '["YES", "NO", "NOT GIVEN"]',
 '["NOT GIVEN"]',
 'Paragraph G mentions Alzheimer''s in context of potential therapies, but does not claim sleep deprivation causes it.'),

('d1000000-0000-0000-0000-000000000026', 'c1000000-0000-0000-0000-000000000002', 26,
 'YES_NO_NOT_GIVEN',
 'Studying before sleep is more effective than staying up all night to study.',
 '["YES", "NO", "NOT GIVEN"]',
 '["YES"]',
 'Paragraph G: "Students who study before sleep and get a full night''s rest are likely to retain more information than those who pull all-night study sessions."');

-- ═══════════════════════════════════════════════════
-- SECTION 3: "Artificial Intelligence in Healthcare" (14 questions)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, instructions)
VALUES ('b1000000-0000-0000-0000-000000000003',
        'a1000000-0000-0000-0000-000000000001', 3,
        'Section 3', 'Read the passage below and answer Questions 27-40.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c1000000-0000-0000-0000-000000000003',
        'b1000000-0000-0000-0000-000000000003', 1,
        'Artificial Intelligence in Healthcare',
        'The integration of artificial intelligence into healthcare represents one of the most promising and challenging developments in modern medicine. From diagnostic imaging to drug discovery, AI technologies are being deployed across virtually every aspect of the healthcare industry, with the potential to improve patient outcomes while reducing costs.

In diagnostic imaging, AI algorithms have demonstrated remarkable accuracy. Deep learning systems can now detect certain cancers, particularly breast cancer and lung cancer, with accuracy rates that match or exceed those of experienced radiologists. A landmark study published in Nature in 2020 showed that an AI system developed by Google Health reduced both false positives and false negatives in breast cancer screening compared to human readers. However, these systems work best as assistive tools rather than replacements for human expertise.

Drug discovery is another area where AI is making significant contributions. Traditional drug development is an extraordinarily lengthy and expensive process, typically taking 10 to 15 years and costing over $2 billion per approved drug. AI can accelerate this process by analyzing vast databases of molecular structures, predicting drug-target interactions, and identifying promising compounds for further testing. During the COVID-19 pandemic, AI tools helped researchers identify potential therapeutic compounds in a fraction of the time that traditional methods would have required.

Electronic health records (EHRs) contain enormous amounts of patient data that are often underutilized. Natural language processing (NLP) algorithms can extract meaningful information from unstructured clinical notes, enabling better clinical decision support. Predictive analytics can identify patients at high risk for conditions such as sepsis, heart failure, or hospital readmission, allowing healthcare providers to intervene before critical events occur.

Despite its promise, AI in healthcare faces considerable obstacles. The quality and representativeness of training data is a fundamental concern. Many AI systems have been trained predominantly on data from Western populations, raising questions about their effectiveness for patients from different ethnic backgrounds. Algorithmic bias can lead to disparities in care quality, particularly for underrepresented groups.

Regulatory frameworks for AI in healthcare are still evolving. Traditional regulatory approaches, designed for static medical devices, may not be appropriate for AI systems that continuously learn and update their algorithms. The FDA has begun developing new frameworks for evaluating AI-based medical technologies, but significant challenges remain in ensuring safety while encouraging innovation.

The question of liability when AI systems make errors is another unresolved issue. If an AI diagnostic tool fails to detect a disease, is the software developer, the healthcare institution, or the treating physician responsible? Clear legal frameworks are needed to address these questions and provide assurance to both healthcare providers and patients.

Privacy and data security represent additional challenges. AI systems require access to large amounts of patient data for training and operation. Ensuring that this data is handled in compliance with regulations such as HIPAA in the United States and GDPR in Europe is essential but complex. The use of techniques such as federated learning, where AI models are trained across multiple institutions without sharing raw patient data, offers a promising approach to this challenge.');

-- Questions 27-30: MULTIPLE CHOICE
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000027', 'c1000000-0000-0000-0000-000000000003', 27,
 'MULTIPLE_CHOICE',
 'The Google Health AI study mentioned in the passage showed that the system',
 '["A. was less accurate than human radiologists", "B. completely replaced human radiologists", "C. reduced errors in breast cancer screening", "D. only worked for lung cancer detection"]',
 '["C"]',
 'Paragraph 2: the AI system "reduced both false positives and false negatives."'),

('d1000000-0000-0000-0000-000000000028', 'c1000000-0000-0000-0000-000000000003', 28,
 'MULTIPLE_CHOICE',
 'According to the passage, traditional drug development typically',
 '["A. takes 5 to 10 years", "B. costs under $1 billion", "C. takes 10 to 15 years and costs over $2 billion", "D. has become faster in recent years without AI"]',
 '["C"]',
 'Paragraph 3: "typically taking 10 to 15 years and costing over $2 billion."'),

('d1000000-0000-0000-0000-000000000029', 'c1000000-0000-0000-0000-000000000003', 29,
 'MULTIPLE_CHOICE',
 'What concern does the passage raise about AI training data?',
 '["A. There is too much data available", "B. It may not represent all populations equally", "C. It is too expensive to collect", "D. Patients refuse to share their data"]',
 '["B"]',
 'Paragraph 5: "trained predominantly on data from Western populations."'),

('d1000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000003', 30,
 'MULTIPLE_CHOICE',
 'Federated learning is described as a way to',
 '["A. replace traditional hospital systems", "B. make AI less accurate but faster", "C. train AI models without sharing raw patient data", "D. eliminate the need for regulations"]',
 '["C"]',
 'Paragraph 8: "trained across multiple institutions without sharing raw patient data."');

-- Questions 31-35: SENTENCE COMPLETION
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000031', 'c1000000-0000-0000-0000-000000000003', 31,
 'SENTENCE_COMPLETION',
 'Deep learning systems can detect cancers with accuracy that matches or exceeds that of experienced _____.',
 '[]',
 '["radiologists"]',
 'Paragraph 2: "accuracy rates that match or exceed those of experienced radiologists."'),

('d1000000-0000-0000-0000-000000000032', 'c1000000-0000-0000-0000-000000000003', 32,
 'SENTENCE_COMPLETION',
 'AI can speed up drug discovery by analyzing databases of molecular _____.',
 '[]',
 '["structures"]',
 'Paragraph 3: "analyzing vast databases of molecular structures."'),

('d1000000-0000-0000-0000-000000000033', 'c1000000-0000-0000-0000-000000000003', 33,
 'SENTENCE_COMPLETION',
 'NLP algorithms extract meaningful information from unstructured clinical _____.',
 '[]',
 '["notes"]',
 'Paragraph 4: "from unstructured clinical notes."'),

('d1000000-0000-0000-0000-000000000034', 'c1000000-0000-0000-0000-000000000003', 34,
 'SENTENCE_COMPLETION',
 'Predictive analytics can identify patients at high risk for conditions such as sepsis, heart failure, or hospital _____.',
 '[]',
 '["readmission"]',
 'Paragraph 4: "sepsis, heart failure, or hospital readmission."'),

('d1000000-0000-0000-0000-000000000035', 'c1000000-0000-0000-0000-000000000003', 35,
 'SENTENCE_COMPLETION',
 'The _____ has begun developing new frameworks for evaluating AI-based medical technologies.',
 '[]',
 '["FDA"]',
 'Paragraph 6: "The FDA has begun developing new frameworks."');

-- Questions 36-40: TRUE / FALSE / NOT GIVEN
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d1000000-0000-0000-0000-000000000036', 'c1000000-0000-0000-0000-000000000003', 36,
 'TRUE_FALSE_NOT_GIVEN',
 'AI diagnostic tools work best when they replace human doctors entirely.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["FALSE"]',
 'Paragraph 2: "these systems work best as assistive tools rather than replacements."'),

('d1000000-0000-0000-0000-000000000037', 'c1000000-0000-0000-0000-000000000003', 37,
 'TRUE_FALSE_NOT_GIVEN',
 'AI was used to help find treatments during the COVID-19 pandemic.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["TRUE"]',
 'Paragraph 3: "During the COVID-19 pandemic, AI tools helped researchers identify potential therapeutic compounds."'),

('d1000000-0000-0000-0000-000000000038', 'c1000000-0000-0000-0000-000000000003', 38,
 'TRUE_FALSE_NOT_GIVEN',
 'Algorithmic bias has already caused patient deaths.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["NOT GIVEN"]',
 'The passage mentions bias concerns but does not state it has caused deaths.'),

('d1000000-0000-0000-0000-000000000039', 'c1000000-0000-0000-0000-000000000003', 39,
 'TRUE_FALSE_NOT_GIVEN',
 'There are clear legal frameworks that determine who is responsible when AI makes medical errors.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["FALSE"]',
 'Paragraph 7: "Clear legal frameworks are needed" — implying they do not yet exist.'),

('d1000000-0000-0000-0000-000000000040', 'c1000000-0000-0000-0000-000000000003', 40,
 'TRUE_FALSE_NOT_GIVEN',
 'HIPAA and GDPR are regulations related to patient data privacy.',
 '["TRUE", "FALSE", "NOT GIVEN"]',
 '["TRUE"]',
 'Paragraph 8 references these regulations in the context of patient data compliance.');


-- ████████████████████████████████████████████████████████████
-- TEST 2: IELTS LISTENING TEST
-- ████████████████████████████████████████████████████████████

INSERT INTO ielts_tests (id, title, skill, time_limit_minutes, difficulty, is_published)
VALUES ('a1000000-0000-0000-0000-000000000002', 'IELTS Listening Practice Test 1', 'LISTENING', 30, 'EASY', true);

-- ═══════════════════════════════════════════════════
-- PART 1: Hotel Booking (10 questions - Form Completion)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, audio_url, instructions)
VALUES ('b2000000-0000-0000-0000-000000000001',
        'a1000000-0000-0000-0000-000000000002', 1,
        'Part 1 - Hotel Reservation',
        'https://example.com/audio/listening-test1-part1.mp3',
        'You will hear a conversation between a hotel receptionist and a guest making a reservation. Complete the form below. Write NO MORE THAN TWO WORDS AND/OR A NUMBER for each answer.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c2000000-0000-0000-0000-000000000001',
        'b2000000-0000-0000-0000-000000000001', 1,
        'Hotel Booking Form', NULL);

INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000001', 'c2000000-0000-0000-0000-000000000001', 1,
 'FORM_COMPLETION', 'Guest name: _____', '[]', '["Sarah Mitchell"]',
 'The guest introduces herself as Sarah Mitchell.'),

('d2000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000001', 2,
 'FORM_COMPLETION', 'Phone number: _____', '[]', '["07845 329167"]',
 'The guest provides this phone number.'),

('d2000000-0000-0000-0000-000000000003', 'c2000000-0000-0000-0000-000000000001', 3,
 'FORM_COMPLETION', 'Check-in date: _____ March', '[]', '["15th"]',
 'The guest says she wants to check in on the 15th of March.'),

('d2000000-0000-0000-0000-000000000004', 'c2000000-0000-0000-0000-000000000001', 4,
 'FORM_COMPLETION', 'Number of nights: _____', '[]', '["3"]',
 'The guest requests a 3-night stay.'),

('d2000000-0000-0000-0000-000000000005', 'c2000000-0000-0000-0000-000000000001', 5,
 'FORM_COMPLETION', 'Room type: _____', '[]', '["double"]',
 'The guest requests a double room.'),

('d2000000-0000-0000-0000-000000000006', 'c2000000-0000-0000-0000-000000000001', 6,
 'FORM_COMPLETION', 'Special request: room with _____ view', '[]', '["garden"]',
 'The guest requests a room with a garden view.'),

('d2000000-0000-0000-0000-000000000007', 'c2000000-0000-0000-0000-000000000001', 7,
 'FORM_COMPLETION', 'Breakfast included: _____', '[]', '["yes"]',
 'The receptionist confirms breakfast is included.'),

('d2000000-0000-0000-0000-000000000008', 'c2000000-0000-0000-0000-000000000001', 8,
 'FORM_COMPLETION', 'Total cost per night: £_____', '[]', '["85"]',
 'The receptionist states the cost is £85 per night.'),

('d2000000-0000-0000-0000-000000000009', 'c2000000-0000-0000-0000-000000000001', 9,
 'FORM_COMPLETION', 'Payment method: _____', '[]', '["credit card"]',
 'The guest chooses to pay by credit card.'),

('d2000000-0000-0000-0000-000000000010', 'c2000000-0000-0000-0000-000000000001', 10,
 'FORM_COMPLETION', 'Confirmation will be sent to email: sarah.mitchell@_____', '[]', '["gmail.com"]',
 'The guest provides her Gmail address.');

-- ═══════════════════════════════════════════════════
-- PART 2: Campus Tour (10 questions - Multiple Choice + Map Labeling)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, audio_url, instructions)
VALUES ('b2000000-0000-0000-0000-000000000002',
        'a1000000-0000-0000-0000-000000000002', 2,
        'Part 2 - University Campus Tour',
        'https://example.com/audio/listening-test1-part2.mp3',
        'You will hear a guide giving a tour of a university campus. Answer Questions 11-20.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c2000000-0000-0000-0000-000000000002',
        'b2000000-0000-0000-0000-000000000002', 1,
        'Campus Tour', NULL);

-- Questions 11-15: MULTIPLE CHOICE
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000011', 'c2000000-0000-0000-0000-000000000002', 11,
 'MULTIPLE_CHOICE',
 'The university library is open until',
 '["A. 8 PM on weekdays", "B. 10 PM on weekdays", "C. midnight every day", "D. 24 hours"]',
 '["B"]',
 'The guide mentions the library closes at 10 PM on weekdays.'),

('d2000000-0000-0000-0000-000000000012', 'c2000000-0000-0000-0000-000000000002', 12,
 'MULTIPLE_CHOICE',
 'The student cafeteria serves',
 '["A. only breakfast and lunch", "B. only lunch and dinner", "C. three meals a day", "D. meals and snacks throughout the day"]',
 '["C"]',
 'The guide says the cafeteria serves three meals a day.'),

('d2000000-0000-0000-0000-000000000013', 'c2000000-0000-0000-0000-000000000002', 13,
 'MULTIPLE_CHOICE',
 'Students can access the sports center',
 '["A. free of charge", "B. with a small monthly fee", "C. only during certain hours", "D. by booking in advance only"]',
 '["A"]',
 'The guide explains that the sports center is free for all enrolled students.'),

('d2000000-0000-0000-0000-000000000014', 'c2000000-0000-0000-0000-000000000002', 14,
 'MULTIPLE_CHOICE',
 'The IT help desk is located',
 '["A. in the main building", "B. next to the library", "C. in the student union", "D. on the second floor of the science block"]',
 '["B"]',
 'The guide mentions the IT help desk is next to the library.'),

('d2000000-0000-0000-0000-000000000015', 'c2000000-0000-0000-0000-000000000002', 15,
 'MULTIPLE_CHOICE',
 'New students must register at the',
 '["A. main reception", "B. student services office", "C. admissions building", "D. online portal only"]',
 '["B"]',
 'The guide directs new students to the student services office for registration.');

-- Questions 16-20: MAP LABELING
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000016', 'c2000000-0000-0000-0000-000000000002', 16,
 'MAP_LABELING',
 'Building at location A on the campus map:',
 '["Student Union", "Science Block", "Library", "Sports Center", "Cafeteria", "Arts Building", "Administration"]',
 '["Library"]',
 'Location A on the map corresponds to the Library as described during the tour.'),

('d2000000-0000-0000-0000-000000000017', 'c2000000-0000-0000-0000-000000000002', 17,
 'MAP_LABELING',
 'Building at location B on the campus map:',
 '["Student Union", "Science Block", "Library", "Sports Center", "Cafeteria", "Arts Building", "Administration"]',
 '["Sports Center"]',
 'Location B on the map corresponds to the Sports Center.'),

('d2000000-0000-0000-0000-000000000018', 'c2000000-0000-0000-0000-000000000002', 18,
 'MAP_LABELING',
 'Building at location C on the campus map:',
 '["Student Union", "Science Block", "Library", "Sports Center", "Cafeteria", "Arts Building", "Administration"]',
 '["Cafeteria"]',
 'Location C on the map corresponds to the Cafeteria.'),

('d2000000-0000-0000-0000-000000000019', 'c2000000-0000-0000-0000-000000000002', 19,
 'MAP_LABELING',
 'Building at location D on the campus map:',
 '["Student Union", "Science Block", "Library", "Sports Center", "Cafeteria", "Arts Building", "Administration"]',
 '["Science Block"]',
 'Location D on the map corresponds to the Science Block.'),

('d2000000-0000-0000-0000-000000000020', 'c2000000-0000-0000-0000-000000000002', 20,
 'MAP_LABELING',
 'Building at location E on the campus map:',
 '["Student Union", "Science Block", "Library", "Sports Center", "Cafeteria", "Arts Building", "Administration"]',
 '["Student Union"]',
 'Location E on the map corresponds to the Student Union.');

-- ═══════════════════════════════════════════════════
-- PART 3: Research Discussion (10 questions - Matching + Sentence Completion)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, audio_url, instructions)
VALUES ('b2000000-0000-0000-0000-000000000003',
        'a1000000-0000-0000-0000-000000000002', 3,
        'Part 3 - Academic Discussion',
        'https://example.com/audio/listening-test1-part3.mp3',
        'You will hear two students discussing a research project. Answer Questions 21-30.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c2000000-0000-0000-0000-000000000003',
        'b2000000-0000-0000-0000-000000000003', 1,
        'Research Project Discussion', NULL);

-- Questions 21-25: MATCHING
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000021', 'c2000000-0000-0000-0000-000000000003', 21,
 'MATCHING',
 'Who will be responsible for the literature review?',
 '["A. Tom", "B. Lisa", "C. Both Tom and Lisa", "D. Neither - the professor will do it"]',
 '["B"]',
 'Lisa volunteers to handle the literature review.'),

('d2000000-0000-0000-0000-000000000022', 'c2000000-0000-0000-0000-000000000003', 22,
 'MATCHING',
 'Who will conduct the interviews?',
 '["A. Tom", "B. Lisa", "C. Both Tom and Lisa", "D. A research assistant"]',
 '["A"]',
 'Tom takes responsibility for conducting the interviews.'),

('d2000000-0000-0000-0000-000000000023', 'c2000000-0000-0000-0000-000000000003', 23,
 'MATCHING',
 'Who will handle data analysis?',
 '["A. Tom", "B. Lisa", "C. Both Tom and Lisa", "D. The professor"]',
 '["C"]',
 'They agree to do the data analysis together.'),

('d2000000-0000-0000-0000-000000000024', 'c2000000-0000-0000-0000-000000000003', 24,
 'MATCHING',
 'Who will write the methodology section?',
 '["A. Tom", "B. Lisa", "C. Both Tom and Lisa", "D. The professor will review it"]',
 '["A"]',
 'Tom agrees to write the methodology section.'),

('d2000000-0000-0000-0000-000000000025', 'c2000000-0000-0000-0000-000000000003', 25,
 'MATCHING',
 'Who will design the questionnaire?',
 '["A. Tom", "B. Lisa", "C. Both Tom and Lisa", "D. The supervisor"]',
 '["B"]',
 'Lisa offers to design the questionnaire.');

-- Questions 26-30: SENTENCE COMPLETION
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000026', 'c2000000-0000-0000-0000-000000000003', 26,
 'SENTENCE_COMPLETION',
 'The students plan to survey a total of _____ participants.',
 '[]', '["150"]',
 'They mention aiming for 150 survey participants.'),

('d2000000-0000-0000-0000-000000000027', 'c2000000-0000-0000-0000-000000000003', 27,
 'SENTENCE_COMPLETION',
 'The research project deadline is the end of _____.',
 '[]', '["November"]',
 'The deadline is mentioned as end of November.'),

('d2000000-0000-0000-0000-000000000028', 'c2000000-0000-0000-0000-000000000003', 28,
 'SENTENCE_COMPLETION',
 'They will use _____ software for statistical analysis.',
 '[]', '["SPSS"]',
 'They agree to use SPSS for the analysis.'),

('d2000000-0000-0000-0000-000000000029', 'c2000000-0000-0000-0000-000000000003', 29,
 'SENTENCE_COMPLETION',
 'The next meeting with their supervisor is on _____.',
 '[]', '["Thursday"]',
 'They confirm their next meeting is on Thursday.'),

('d2000000-0000-0000-0000-000000000030', 'c2000000-0000-0000-0000-000000000003', 30,
 'SENTENCE_COMPLETION',
 'The research topic focuses on the impact of _____ on student performance.',
 '[]', '["social media"]',
 'The research investigates the impact of social media on student performance.');

-- ═══════════════════════════════════════════════════
-- PART 4: Lecture on Climate Change (10 questions - Multiple Choice + Sentence Completion)
-- ═══════════════════════════════════════════════════

INSERT INTO ielts_sections (id, test_id, section_order, title, audio_url, instructions)
VALUES ('b2000000-0000-0000-0000-000000000004',
        'a1000000-0000-0000-0000-000000000002', 4,
        'Part 4 - Academic Lecture',
        'https://example.com/audio/listening-test1-part4.mp3',
        'You will hear a lecture on climate change and its effects on coastal communities. Answer Questions 31-40.');

INSERT INTO ielts_passages (id, section_id, passage_order, title, content)
VALUES ('c2000000-0000-0000-0000-000000000004',
        'b2000000-0000-0000-0000-000000000004', 1,
        'Climate Change and Coastal Communities', NULL);

-- Questions 31-35: MULTIPLE CHOICE
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000031', 'c2000000-0000-0000-0000-000000000004', 31,
 'MULTIPLE_CHOICE',
 'The main topic of the lecture is',
 '["A. global warming in general", "B. the effects of climate change on coastal areas", "C. how to prevent climate change", "D. the history of coastal settlements"]',
 '["B"]',
 'The lecture focuses on climate change effects on coastal communities.'),

('d2000000-0000-0000-0000-000000000032', 'c2000000-0000-0000-0000-000000000004', 32,
 'MULTIPLE_CHOICE',
 'Sea levels are expected to rise by the end of the century by',
 '["A. 10-20 cm", "B. 20-40 cm", "C. 30-60 cm", "D. 50-100 cm"]',
 '["D"]',
 'The lecturer states sea levels could rise by 50-100 cm by the end of the century.'),

('d2000000-0000-0000-0000-000000000033', 'c2000000-0000-0000-0000-000000000004', 33,
 'MULTIPLE_CHOICE',
 'The most vulnerable region mentioned in the lecture is',
 '["A. Northern Europe", "B. South Asia", "C. North America", "D. Sub-Saharan Africa"]',
 '["B"]',
 'The lecturer identifies South Asia as particularly vulnerable.'),

('d2000000-0000-0000-0000-000000000034', 'c2000000-0000-0000-0000-000000000004', 34,
 'MULTIPLE_CHOICE',
 'One adaptation strategy discussed is',
 '["A. relocating all coastal populations", "B. building higher seawalls", "C. managed retreat from coastlines", "D. banning coastal development entirely"]',
 '["C"]',
 'The lecturer discusses managed retreat as an adaptation strategy.'),

('d2000000-0000-0000-0000-000000000035', 'c2000000-0000-0000-0000-000000000004', 35,
 'MULTIPLE_CHOICE',
 'According to the lecturer, the economic cost of coastal flooding worldwide could reach',
 '["A. $1 billion annually", "B. $10 billion annually", "C. $100 billion annually", "D. $1 trillion annually"]',
 '["D"]',
 'The lecturer mentions costs could reach $1 trillion annually.');

-- Questions 36-40: SENTENCE COMPLETION
INSERT INTO ielts_questions (id, passage_id, question_order, question_type, question_text, options, correct_answers, explanation) VALUES
('d2000000-0000-0000-0000-000000000036', 'c2000000-0000-0000-0000-000000000004', 36,
 'SENTENCE_COMPLETION',
 'Approximately _____ percent of the world''s population lives in coastal areas.',
 '[]', '["40"]',
 'The lecturer states about 40% of people live in coastal areas.'),

('d2000000-0000-0000-0000-000000000037', 'c2000000-0000-0000-0000-000000000004', 37,
 'SENTENCE_COMPLETION',
 'Rising sea levels are primarily caused by thermal expansion and melting _____.',
 '[]', '["glaciers"]',
 'The lecturer explains thermal expansion and melting glaciers as the main causes.'),

('d2000000-0000-0000-0000-000000000038', 'c2000000-0000-0000-0000-000000000004', 38,
 'SENTENCE_COMPLETION',
 'Saltwater intrusion threatens coastal _____ supplies.',
 '[]', '["freshwater"]',
 'The lecturer discusses saltwater intrusion into freshwater supplies.'),

('d2000000-0000-0000-0000-000000000039', 'c2000000-0000-0000-0000-000000000004', 39,
 'SENTENCE_COMPLETION',
 'The Netherlands is often cited as a model for coastal _____ engineering.',
 '[]', '["flood"]',
 'The lecturer mentions the Netherlands as a model for flood engineering.'),

('d2000000-0000-0000-0000-000000000040', 'c2000000-0000-0000-0000-000000000004', 40,
 'SENTENCE_COMPLETION',
 'Small island nations in the _____ are among the most threatened by rising sea levels.',
 '[]', '["Pacific"]',
 'The lecturer highlights Pacific island nations as most threatened.');
