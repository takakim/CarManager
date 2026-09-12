from PIL import Image, ImageDraw, ImageFont
import os

os.makedirs("play_store_graphics", exist_ok=True)

# 1. App Icon (512x512)
icon = Image.new("RGBA", (512, 512), (11, 17, 32, 255)) # Dark background #0B1120
draw = ImageDraw.Draw(icon)

# Draw rounded rectangle background
draw.rounded_rectangle([40, 40, 472, 472], radius=96, fill=(19, 29, 49, 255), outline=(52, 211, 153, 255), width=6)

# Inner fuel pump / energy symbol
draw.rounded_rectangle([180, 160, 332, 380], radius=24, fill=(4, 120, 87, 255))
draw.rounded_rectangle([204, 184, 308, 240], radius=12, fill=(249, 115, 22, 255))
draw.arc([280, 220, 380, 340], start=270, end=90, fill=(56, 189, 248, 255), width=12)

# Lightning bolt (energy) overlay
points = [(260, 200), (230, 270), (270, 270), (240, 340), (290, 250), (255, 250)]
draw.polygon(points, fill=(251, 191, 36, 255))

icon.save("play_store_graphics/app_icon_512x512.png")
print("Saved app_icon_512x512.png")

# 2. Feature Graphic (1024x500)
banner = Image.new("RGBA", (1024, 500), (11, 17, 32, 255))
draw_banner = ImageDraw.Draw(banner)

draw_banner.rectangle([0, 0, 1024, 500], fill=(11, 17, 32, 255))
draw_banner.ellipse([-100, -100, 400, 400], fill=(4, 120, 87, 40))
draw_banner.ellipse([700, 200, 1124, 600], fill=(2, 132, 199, 40))

try:
    font_title = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 54)
    font_sub = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 26)
except:
    font_title = ImageFont.load_default()
    font_sub = ImageFont.load_default()

draw_banner.text((80, 160), "Fuel & Energy Tracker", fill=(241, 245, 249, 255), font=font_title)
draw_banner.text((80, 235), "Track Fuel, EV Charging, Spending & AI Insights", fill=(52, 211, 153, 255), font=font_sub)

draw_banner.rounded_rectangle([80, 300, 360, 350], radius=12, fill=(249, 115, 22, 255))
try:
    font_pill = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 18)
except:
    font_pill = ImageFont.load_default()
draw_banner.text((105, 313), "Smart Automotive Analytics", fill=(255, 255, 255, 255), font=font_pill)

banner.save("play_store_graphics/feature_graphic_1024x500.png")
print("Saved feature_graphic_1024x500.png")

# 3. Screenshot 1 - Dashboard Mockup (1080x1920)
ss1 = Image.new("RGBA", (1080, 1920), (11, 17, 32, 255))
draw_ss1 = ImageDraw.Draw(ss1)
draw_ss1.rectangle([0, 0, 1080, 160], fill=(19, 29, 49, 255))
try:
    font_header = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 40)
    font_body = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 28)
except:
    font_header = ImageFont.load_default()
    font_body = ImageFont.load_default()

draw_ss1.text((60, 60), "Fuel & Energy Tracker", fill=(241, 245, 249, 255), font=font_header)

draw_ss1.rounded_rectangle([60, 200, 1020, 520], radius=32, fill=(19, 29, 49, 255), outline=(52, 211, 153, 255), width=3)
draw_ss1.text((100, 240), "Total Spending & Efficiency", fill=(52, 211, 153, 255), font=font_header)
draw_ss1.text((100, 320), "$1,245.50 Total Spent YTD", fill=(241, 245, 249, 255), font=font_body)
draw_ss1.text((100, 380), "7.8 L/100km Average Economy", fill=(56, 189, 248, 255), font=font_body)
draw_ss1.text((100, 440), "34.2 MPG (US) | 12.8 km/L", fill=(148, 163, 184, 255), font=font_body)

draw_ss1.rounded_rectangle([60, 560, 1020, 920], radius=32, fill=(19, 29, 49, 255), outline=(249, 115, 22, 255), width=3)
draw_ss1.text((100, 600), "🤖 On-Device Smart Advisor", fill=(249, 115, 22, 255), font=font_header)
draw_ss1.text((100, 680), "Efficiency Score: 92/100 (Optimal)", fill=(241, 245, 249, 255), font=font_body)
draw_ss1.text((100, 740), "• Fuel economy improved by 4.2% this month.", fill=(148, 163, 184, 255), font=font_body)
draw_ss1.text((100, 800), "• Next maintenance recommended in 1,200 km.", fill=(148, 163, 184, 255), font=font_body)

draw_ss1.rounded_rectangle([60, 960, 1020, 1320], radius=32, fill=(19, 29, 49, 255))
draw_ss1.text((100, 1000), "Recent Fuel & Energy Logs", fill=(241, 245, 249, 255), font=font_header)
draw_ss1.text((100, 1080), "Oct 24 • Regular Gasoline • 42.5 L • $68.00", fill=(241, 245, 249, 255), font=font_body)
draw_ss1.text((100, 1140), "Oct 15 • EV Fast Charge • 35 kWh • $14.50", fill=(241, 245, 249, 255), font=font_body)

ss1.save("play_store_graphics/screenshot_dashboard_1080x1920.png")
print("Saved screenshot_dashboard_1080x1920.png")

# 4. Screenshot 2 - Analytics Mockup (1080x1920)
ss2 = Image.new("RGBA", (1080, 1920), (11, 17, 32, 255))
draw_ss2 = ImageDraw.Draw(ss2)
draw_ss2.rectangle([0, 0, 1080, 160], fill=(19, 29, 49, 255))
draw_ss2.text((60, 60), "Spending Analytics", fill=(241, 245, 249, 255), font=font_header)

draw_ss2.rounded_rectangle([60, 200, 1020, 650], radius=32, fill=(19, 29, 49, 255), outline=(2, 132, 199, 40), width=3)
draw_ss2.text((100, 240), "Monthly Spending Trend", fill=(56, 189, 248, 255), font=font_header)

bars = [120, 210, 180, 290, 240, 310]
months = ["May", "Jun", "Jul", "Aug", "Sep", "Oct"]
for i, (val, month) in enumerate(zip(bars, months)):
    x = 140 + i * 140
    y_bottom = 560
    y_top = 560 - val
    draw_ss2.rounded_rectangle([x, y_top, x + 80, y_bottom], radius=12, fill=(4, 120, 87, 255))
    draw_ss2.text((x + 15, y_bottom + 15), month, fill=(148, 163, 184, 255), font=font_body)

draw_ss2.rounded_rectangle([60, 700, 1020, 1150], radius=32, fill=(19, 29, 49, 255))
draw_ss2.text((100, 740), "Fuel Price History ($/L)", fill=(241, 245, 249, 255), font=font_header)
draw_ss2.text((100, 820), "Average Unit Price: $1.58 / L", fill=(52, 211, 153, 255), font=font_body)
draw_ss2.text((100, 880), "Lowest Price Recorded: $1.45 / L", fill=(148, 163, 184, 255), font=font_body)
draw_ss2.text((100, 940), "Highest Price Recorded: $1.72 / L", fill=(148, 163, 184, 255), font=font_body)

ss2.save("play_store_graphics/screenshot_analytics_1080x1920.png")
print("Saved screenshot_analytics_1080x1920.png")
print("All store graphics generated successfully!")
