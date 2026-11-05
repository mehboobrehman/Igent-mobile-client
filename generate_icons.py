
from PIL import Image, ImageDraw

def create_round_icon(image, size):
    # Resize the image
    image = image.resize(size, Image.Resampling.LANCZOS)

    # Create a circular mask
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)

    # Apply the mask
    round_image = Image.new('RGBA', size, (0, 0, 0, 0))
    round_image.paste(image, (0, 0), mask)
    return round_image

def generate_icons(ico_path, output_base_path):
    # Load the original ICO file and convert to RGBA
    original_icon = Image.open(ico_path).convert("RGBA")

    # Define target densities and sizes
    sizes = {
        "mdpi": (48, 48),
        "hdpi": (72, 72),
        "xhdpi": (96, 96),
        "xxhdpi": (144, 144),
        "xxxhdpi": (192, 192),
    }

    for density, size in sizes.items():
        # Create output directories if they don't exist
        mipmap_dir = f"{output_base_path}/mipmap-{density}"
        import os
        os.makedirs(mipmap_dir, exist_ok=True)

        # Generate ic_launcher.png (square)
        square_icon = original_icon.resize(size, Image.Resampling.LANCZOS)
        square_icon.save(f"{mipmap_dir}/ic_launcher.png")
        print(f"Generated {mipmap_dir}/ic_launcher.png ({size[0]}x{size[1]})")

        # Generate ic_launcher_round.png (circular)
        round_icon = create_round_icon(original_icon, size)
        round_icon.save(f"{mipmap_dir}/ic_launcher_round.png")
        print(f"Generated {mipmap_dir}/ic_launcher_round.png ({size[0]}x{size[1]})")

if __name__ == "__main__":
    project_root = r'C:\Users\mehboob.rehman\OneDrive - National Foods Limited\AI\autodesk\Autodeskprojects\igent'
    ico_file = f"{project_root}/icon.ico"
    output_res_path = f"{project_root}/app/src/main/res"
    generate_icons(ico_file, output_res_path)
