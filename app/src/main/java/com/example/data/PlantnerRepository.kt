package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class PlantnerRepository(private val db: AppDatabase) {
    private val farmDao = db.farmProfileDao()
    private val scanDao = db.plantScanDao()
    private val sensorDao = db.sensorReadingDao()
    private val chatDao = db.chatMessageDao()
    private val guideDao = db.cropGuideDao()

    val farmProfile: Flow<FarmProfileEntity?> = farmDao.getFarmProfile()
    val allScans: Flow<List<PlantScanEntity>> = scanDao.getAllScans()
    val recentReadings: Flow<List<SensorReading>> = sensorDao.getRecentReadings(30)
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allGuides: Flow<List<CropGuideEntity>> = guideDao.getAllGuides()

    suspend fun getGuide(cropName: String, stage: String): CropGuideEntity? {
        return guideDao.getGuide(cropName, stage)
    }

    suspend fun insertGuide(guide: CropGuideEntity) {
        guideDao.insertGuide(guide)
    }

    suspend fun clearGuides() {
        guideDao.clearGuides()
    }

    suspend fun seedDefaultCropGuidesIfEmpty() {
        val current = guideDao.getAllGuides().firstOrNull()
        if (current.isNullOrEmpty()) {
            val list = listOf(
                // Basil
                CropGuideEntity(
                    cropName = "Basil",
                    stage = "Seedling",
                    practices = "Mist soil gently with tepid water - keep upper rootzone aerated.|||Provide 14 hours of soft nursery light to prevent weak leggy stems.|||Keep seedlings at 21-24°C; shield from chilly window panes or drafts.",
                    diagnosis = "Damping-off fungal rot. Emerged seedlings thin drastically at soil margin, collapse, and rot. Hand-dust shallow charcoal dust onto soil surface to dry fungal filaments.",
                    basePriceUsd = 4.20,
                    stageMultiplier = 0.15,
                    baseYieldPerAcreKg = 3000.0
                ),
                CropGuideEntity(
                    cropName = "Basil",
                    stage = "Vegetative",
                    practices = "Pinch terminal shoot tips once stems reach 15cm to stimulate thick branching.|||Harvest outer lower leaves to redirect carbohydrate reserves to inner nodes.|||Feed weak liquid biological seaweed every 14 days during warm soil periods.",
                    diagnosis = "Downy Mildew & Fusarium wilt. Pale yellow contours blotch the upper leaves while dusty white/purple spore down emerges beneath. Trim inner secondary branches to decrease microclimatic humidity.",
                    basePriceUsd = 4.20,
                    stageMultiplier = 0.60,
                    baseYieldPerAcreKg = 3000.0
                ),
                CropGuideEntity(
                    cropName = "Basil",
                    stage = "Flowering",
                    practices = "Pinch off emerging delicate flower spikes immediately to maintain aromatic leaf sugars.|||Water base daily but do not spray the tender leaves to keep foliage spore-free.|||Add well-matured organic compost around stem borders to bolster continuous leaf flushes.",
                    diagnosis = "Leaf Miners / Aphids. Sneaky, wavy white trails are mined across upper leaf cuticles. Discard infested foliage, apply dilute biodegradable dish soap solution to suffocate vectors.",
                    basePriceUsd = 4.20,
                    stageMultiplier = 0.85,
                    baseYieldPerAcreKg = 3000.0
                ),
                CropGuideEntity(
                    cropName = "Basil",
                    stage = "Mature",
                    practices = "Harvest whole mature branches in early morning when essential oil indices peak.|||Pinch flowering heads daily; maintain high soil moisture to support high leaf canopy density.|||Trim root perimeter weeds to assure soil nutrition focus remains entirely on Basil.",
                    diagnosis = "Botrytis Gray Mold. Lower stems decay turning spongy, grey, and mouldy due to overcrowding. Space pots 20cm apart to improve solar penetration.",
                    basePriceUsd = 4.20,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 3000.0
                ),
                // Cassava
                CropGuideEntity(
                    cropName = "Cassava",
                    stage = "Seedling",
                    practices = "Select disease-resilient stakes and plant slanted at 45° in rich soil mounds.|||Keep weeding area completely clear within 30cm of stakes to minimize early root struggle.|||Monitor early soil moisture - avoid severe dry stress until rooting takes off.",
                    diagnosis = "Stem-borer invasion. Larvae tunnel the wood cuticles making stakes dry and bend. Wrap stem cuts with clean organic ash to shield from bore entrance.",
                    basePriceUsd = 0.18,
                    stageMultiplier = 0.10,
                    baseYieldPerAcreKg = 9000.0
                ),
                CropGuideEntity(
                    cropName = "Cassava",
                    stage = "Vegetative",
                    practices = "Keep high weed-free zone around main root mounds using mulch or hand-weeding.|||Ensure healthy branch structure; prune down secondary stunted suckers to form proper top crown.|||Intercrop with low-foliage cover crops like beans to naturally lock in Nitrogen.",
                    diagnosis = "Cassava Mosaic Disease (CMD). Leaves become narrow, distorted, puckered and chlorotic green-yellow. Select resistant cuttings next season and immediately pull and burn severely infected CMD stems to protect your farm boundary.",
                    basePriceUsd = 0.18,
                    stageMultiplier = 0.50,
                    baseYieldPerAcreKg = 9000.0
                ),
                CropGuideEntity(
                    cropName = "Cassava",
                    stage = "Flowering",
                    practices = "Ensure deep soil mulching around mounds to facilitate lateral root tuberization.|||Apply natural wood-ash compost around root crown to boost potassium indices.|||Prune down excessive dense top branch cover to allow sunlight to penetrate lower leaves.",
                    diagnosis = "Brown Leaf Spot. Dark brown polygonal lesions form on older lower leaves, triggering premature leaf drop. Improve airflow and spray organic copper-based solutions on lower tiers.",
                    basePriceUsd = 0.18,
                    stageMultiplier = 0.80,
                    baseYieldPerAcreKg = 9000.0
                ),
                CropGuideEntity(
                    cropName = "Cassava",
                    stage = "Mature",
                    practices = "Stop applying compost 30 days before harvesting to consolidate starch density.|||Prune upper stalks 2 weeks prior to root harvest - this triggers starch concentration in tubers.|||Carefully dislodge soil with a hand trowel; extract tubers without wounding outer skins.",
                    diagnosis = "Cassava Root Rot. Under-drainage causes tubers to turn brown, spongy and rot with a bad smell first noticeable via leaf wilting. Ensure sandy-loam grading.",
                    basePriceUsd = 0.18,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 9000.0
                ),
                // Coffee
                CropGuideEntity(
                    cropName = "Coffee",
                    stage = "Seedling",
                    practices = "Filter direct sunlight using a 50% shade nursery netting system.|||Water thoroughly in early mornings; verify potting bags allow free drainage.|||Keep seedlings raised off direct soil floor to eliminate nematode infection risk.",
                    diagnosis = "Damping-off & Pythium. Seedling stems blacken near soil level, collapse and die. Ensure sterilised loam sand mixes and do not over-irrigate.",
                    basePriceUsd = 3.80,
                    stageMultiplier = 0.10,
                    baseYieldPerAcreKg = 1500.0
                ),
                CropGuideEntity(
                    cropName = "Coffee",
                    stage = "Vegetative",
                    practices = "Prune secondary vertical suckers early to form a balanced main trunk crown.|||Dig wide organic compost rings around the drip-line of the coffee shrubbery.|||Control grass weeds around shallow rootzones using organic mulch barriers.",
                    diagnosis = "Mealybugs. Creamy, cottony white masses suck sap from fresh green shoots and leaf nodes. Encourage ladybugs and apply oil washes manually.",
                    basePriceUsd = 3.80,
                    stageMultiplier = 0.45,
                    baseYieldPerAcreKg = 1500.0
                ),
                CropGuideEntity(
                    cropName = "Coffee",
                    stage = "Flowering",
                    practices = "Avoid spraying any leaves during initial blossom emergence to prevent bud drop.|||Maintain solid moisture inputs; dry-season irrigation triggers uniform white blossoming.|||Incorporate organic bone meal to furnish Phosphorus for high bud retention.",
                    diagnosis = "Coffee Berry Borer (CBB). Tiny beetles bore small entry holes right in the apex of green berries. Prune late hanging cherries and carry out intensive farm clearing.",
                    basePriceUsd = 3.80,
                    stageMultiplier = 0.75,
                    baseYieldPerAcreKg = 1500.0
                ),
                CropGuideEntity(
                    cropName = "Coffee",
                    stage = "Mature",
                    practices = "Precisely hand-pick only fully red, ripe cherries to secure superior quality scores.|||Spread cherry skins back onto the coffee tree base to recycle potassium trace elements.|||Carefully prune dead internal branches post-harvest to ventilate the tree cores.",
                    diagnosis = "Coffee Leaf Rust (CLR). Dusty orange/yellow powdery pustules cover the undersides of mature leaves, causing leaf drop. Apply preventive copper fungicides during humid flushes.",
                    basePriceUsd = 3.80,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 1500.0
                ),
                // Cocoa
                CropGuideEntity(
                    cropName = "Cocoa",
                    stage = "Seedling",
                    practices = "Establish high shade canopy using banana trees or shade fabrics in nurseries.|||Mist soil daily; young Cocoa roots are highly sensitive to prolonged dry spells.|||Place protective windbreakers to prevent draft damage on tender new leaves.",
                    diagnosis = "Anthracnose leaf blast. Copper-red spots with yellow margins burn soft flush leaves. Ensure healthy canopy shade and dust with organic copper dust.",
                    basePriceUsd = 5.50,
                    stageMultiplier = 0.10,
                    baseYieldPerAcreKg = 800.0
                ),
                CropGuideEntity(
                    cropName = "Cocoa",
                    stage = "Vegetative",
                    practices = "Prune low-hanging branches and chupons (water shoots) to create a clean main trunk.|||Keep under-canopy leaf litter intact to conserve humidity but prevent soil mold.|||Apply potassium-rich organic fertilizers around the trunk circle.",
                    diagnosis = "Mirids / Capsids. Sucking pests bite soft stem bark causing dark sunken wounds that die back. Keep canopy open to let light suppress humid pest niches.",
                    basePriceUsd = 5.50,
                    stageMultiplier = 0.40,
                    baseYieldPerAcreKg = 800.0
                ),
                CropGuideEntity(
                    cropName = "Cocoa",
                    stage = "Flowering",
                    practices = "Conserve leaf litter beneath the trees to sustain pollinator midge populations.|||Avoid all broad-spectrum insecticide sprays that might eliminate midge vectors.|||Remove any side shoots growing too close to young flower cushions.",
                    diagnosis = "Black Pod Disease (Phytophthora). Water-soaked dark brown spots rot green pods, producing a dusty velvet white fungal rim. Prune overhanging foliage to reduce moisture.",
                    basePriceUsd = 5.50,
                    stageMultiplier = 0.75,
                    baseYieldPerAcreKg = 800.0
                ),
                CropGuideEntity(
                    cropName = "Cocoa",
                    stage = "Mature",
                    practices = "Harvest ripe golden pods using dynamic pruning shears - never pull the pod directly.|||Avoid wounding the bark cushions; flowers for subsequent seasons emerge there.|||Extract wet cocoa beans same day and begin immediate banana leaf sweat fermentation.",
                    diagnosis = "Witches' Broom. Abnormal, bunchy shoot growth containing dense dried twigs emerge on fan branches due to fungal spores. Clip and destroy infected brooms.",
                    basePriceUsd = 5.50,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 800.0
                ),
                // Tomato
                CropGuideEntity(
                    cropName = "Tomato",
                    stage = "Seedling",
                    practices = "Water base gently at seedling crowns - avoid flattening small stems.|||Transplant seedlings in evening hours once 4-6 master leaflets display.|||Handle tender tomato cubes by their rootballs to minimize shock impact.",
                    diagnosis = "Damping-Off disease. Stems thin out, pinch at base and fall over overnight. Minimize seed density and apply warm, well-ventilated watering practices.",
                    basePriceUsd = 1.20,
                    stageMultiplier = 0.10,
                    baseYieldPerAcreKg = 12000.0
                ),
                CropGuideEntity(
                    cropName = "Tomato",
                    stage = "Vegetative",
                    practices = "Install vertical wooden stakes immediately and loosely secure growing vines.|||Pinch out lateral suckers (shoots at branch joints) to build strong single stems.|||Prune off bottom-most leaves touch the soil to prevent splashing blight spores.",
                    diagnosis = "Septoria Leaf Spot. Small circular spots with pale grey centers and tiny black specs on lower foliage. Mulch soil to avoid mud splash.",
                    basePriceUsd = 1.20,
                    stageMultiplier = 0.50,
                    baseYieldPerAcreKg = 12000.0
                ),
                CropGuideEntity(
                    cropName = "Tomato",
                    stage = "Flowering",
                    practices = "Add high Calcium/Bone meal to soil to forestall calcium deficiency rot.|||Water deep and steady; irregular soaking triggers blossom drop and split fruit skins.|||Gently shake stakes on calm warm afternoons to aid wind-assisted self-pollination.",
                    diagnosis = "Blossom End Rot. A dry, black, flat leathery patch decays the blossom bottom of green tomatoes. Water uniformly and ensure sound calcium absorption.",
                    basePriceUsd = 1.20,
                    stageMultiplier = 0.80,
                    baseYieldPerAcreKg = 12000.0
                ),
                CropGuideEntity(
                    cropName = "Tomato",
                    stage = "Mature",
                    practices = "Pick fruits at 'breaker' stage (showing first pink blush) to let vine sugars feed newer clusters.|||Decrease watering once clusters mature to concentrate sugars and prevent split skin.|||Prune yellowed outer foliage to direct remaining plant energy strictly to ripening fruit.",
                    diagnosis = "Late Blight. Rapidly spreading, greasy black patches on leaves and fruit with white downy undersides during damp days. Prop up vines and apply protective soap treatments.",
                    basePriceUsd = 1.20,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 12000.0
                ),
                // Maize / Others
                CropGuideEntity(
                    cropName = "Maize",
                    stage = "Seedling",
                    practices = "Maintain shallow weed-free lines; Maize roots have low competitiveness early on.|||Sow seeds at 3-5cm depth; verify moisture reaches seed lines.|||Apply a starter organic compost around seedling furrows to spur root growth.",
                    diagnosis = "Cutworms. Young seedlings are found neatly severed flat on the soil line in early morning. Circle small seedling stems with ash to deter soil pests.",
                    basePriceUsd = 0.35,
                    stageMultiplier = 0.10,
                    baseYieldPerAcreKg = 4500.0
                ),
                CropGuideEntity(
                    cropName = "Maize",
                    stage = "Vegetative",
                    practices = "Top-dress nitrogen compost/compost tea during the crucial V4 to V6 development weeks.|||Keep crop lines weed-free to maximize light collection by early leaves.|||Ensure solid watering as daily stem height expansion accelerates.",
                    diagnosis = "Fall Armyworm. Ragged chewing holes in whorl margins with frass. Spray organic neem oil directly into the central leaf funnel.",
                    basePriceUsd = 0.35,
                    stageMultiplier = 0.55,
                    baseYieldPerAcreKg = 4500.0
                ),
                CropGuideEntity(
                    cropName = "Maize",
                    stage = "Flowering",
                    practices = "Provide uninterrupted moisture inputs during tasseling and silking to prevent dry kernels.|||Refrain from mechanical weeding to prevent disturbing fragile lateral roots.|||Add potassium-rich fertilizer around rootbeds to bolster grain filling.",
                    diagnosis = "Maize Smut. Swelling greyish galls on ears or tassels that rupture releasing black soot spores. Cut and bury infected galls far from fields before they burst.",
                    basePriceUsd = 0.35,
                    stageMultiplier = 0.85,
                    baseYieldPerAcreKg = 4500.0
                ),
                CropGuideEntity(
                    cropName = "Maize",
                    stage = "Mature",
                    practices = "Wait until black milk-line layer forms at grain attachments to signal maximum starch maturity.|||Allow cobs to dry slightly on standing stalks pre-harvest.|||Store harvested grains at under 13% internal moisture to prevent high storage mold risks.",
                    diagnosis = "Aspergillus/Gibberella Ear Rot. Grains turn pinkish or green with powdery molds due to ear insect entry wounds. Keep storage containers aerated and completely moisture-free.",
                    basePriceUsd = 0.35,
                    stageMultiplier = 1.00,
                    baseYieldPerAcreKg = 4500.0
                )
            )
            guideDao.insertGuides(list)
        }
    }

    suspend fun getScanById(id: Int): Flow<PlantScanEntity?> {
        return scanDao.getScanById(id)
    }

    suspend fun updateFarmProfile(profile: FarmProfileEntity) {
        farmDao.insertOrUpdate(profile)
    }

    suspend fun ensureDefaultFarmProfile() {
        val current = farmProfile.firstOrNull()
        if (current == null) {
            farmDao.insertOrUpdate(
                FarmProfileEntity(
                    id = 1,
                    farmName = "Valley Green Farms",
                    location = "Rift Valley, Kenya",
                    farmSize = "24 Acres",
                    primaryCrops = "Tomato, Leafy Greens, Bell Peppers, Corn",
                    equipment = "John Deere 5075E, Automated Drip irrigation, Soil probes",
                    budget = 5000.0,
                    region = "Kenya",
                    phoneNumber = "+254 712 345678"
                )
            )
        }
    }

    suspend fun insertScan(scan: PlantScanEntity): Long {
        return scanDao.insertScan(scan)
    }

    suspend fun deleteScan(scan: PlantScanEntity) {
        scanDao.deleteScan(scan)
    }

    suspend fun updateScanResolution(id: Int, isResolved: Boolean) {
        scanDao.updateScanResolution(id, isResolved)
    }

    suspend fun insertReading(reading: SensorReading) {
        sensorDao.insertReading(reading)
    }

    suspend fun generateNewSensorReading() {
        // Simulates real-time dynamics for humidity, soil moisture, and temperature
        val lastReadings = recentReadings.firstOrNull()
        val prevTemp = lastReadings?.firstOrNull()?.temperature ?: 24.5f
        val prevHumid = lastReadings?.firstOrNull()?.humidity ?: 55.0f
        val prevMoisture = lastReadings?.firstOrNull()?.soilMoisture ?: 42.0f

        val nextTemp = (prevTemp + Random.nextDouble(-0.5, 0.5).toFloat()).coerceIn(15.0f, 38.0f)
        val nextHumid = (prevHumid + Random.nextDouble(-1.0, 1.0).toFloat()).coerceIn(30.0f, 95.0f)
        val nextMoisture = (prevMoisture + Random.nextDouble(-1.5, 1.5).toFloat()).coerceIn(10.0f, 100.0f)

        sensorDao.insertReading(
            SensorReading(
                temperature = nextTemp,
                humidity = nextHumid,
                soilMoisture = nextMoisture
            )
        )
    }

    suspend fun seedMockSensorDataIfEmpty() {
        val current = recentReadings.firstOrNull()
        if (current.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            var temp = 24.5f
            var humid = 55.0f
            var moisture = 45.0f
            // Seed 12 points (e.g. 12 hours check)
            for (i in 11 downTo 0) {
                temp += Random.nextDouble(-1.5, 1.5).toFloat()
                humid += Random.nextDouble(-3.0, 3.0).toFloat()
                moisture += Random.nextDouble(-3.5, 3.5).toFloat()
                
                temp = temp.coerceIn(15.0f, 35.0f)
                humid = humid.coerceIn(30.0f, 90.0f)
                moisture = moisture.coerceIn(10.0f, 90.0f)

                sensorDao.insertReading(
                    SensorReading(
                        timestamp = now - (i * 3600 * 1000),
                        temperature = temp,
                        humidity = humid,
                        soilMoisture = moisture
                    )
                )
            }
        }
    }

    suspend fun insertMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChat() {
        chatDao.clearChat()
    }

    suspend fun clearReadings() {
        sensorDao.clearReadings()
    }
}
